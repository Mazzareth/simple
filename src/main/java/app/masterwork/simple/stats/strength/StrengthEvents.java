package app.masterwork.simple.stats.strength;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import app.masterwork.simple.stats.PlayerStats;

public final class StrengthEvents {
    private static final long BLOCK_ACTION_COOLDOWN_TICKS = 40L;
    private static final long PENDING_PLACEMENT_EXPIRY_TICKS = 2L;
    private static final StrengthActionTracker ACTION_TRACKER = new StrengthActionTracker(BLOCK_ACTION_COOLDOWN_TICKS, 32);
    private static final Map<UUID, List<PendingPlacement>> PENDING_PLACEMENTS = new HashMap<>();
    private static boolean registered;

    private StrengthEvents() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        ServerLivingEntityEvents.AFTER_DAMAGE.register(StrengthEvents::afterDamage);
        ServerLivingEntityEvents.AFTER_DEATH.register(StrengthEvents::afterDeath);
        PlayerBlockBreakEvents.AFTER.register(StrengthEvents::afterBlockBreak);
        UseBlockCallback.EVENT.register(StrengthEvents::beforeUseBlock);
        ServerTickEvents.END_SERVER_TICK.register(StrengthEvents::flushPendingPlacements);
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> {
            ACTION_TRACKER.clear(newPlayer.getUUID());
            PENDING_PLACEMENTS.remove(newPlayer.getUUID());
        });
        ServerPlayerEvents.LEAVE.register(player -> {
            ACTION_TRACKER.clear(player.getUUID());
            PENDING_PLACEMENTS.remove(player.getUUID());
        });
    }

    private static void afterDamage(LivingEntity entity, DamageSource source, float baseDamageTaken, float damageTaken, boolean blocked) {
        if (damageTaken <= 0.0F) {
            return;
        }

        ServerPlayer player = meleeAttacker(source);

        if (player == null || !canGainStrength(player)) {
            return;
        }

        PlayerStats.awardStrengthXp(player, StrengthExperience.meleeDamageXp(damageTaken));
    }

    private static void afterDeath(LivingEntity entity, DamageSource damageSource) {
        ServerPlayer player = meleeAttacker(damageSource);

        if (player == null || !canGainStrength(player)) {
            return;
        }

        PlayerStats.awardStrengthXp(player, StrengthExperience.meleeKillXp());
    }

    private static void afterBlockBreak(Level level, Player player, BlockPos pos, BlockState state, net.minecraft.world.level.block.entity.BlockEntity blockEntity) {
        if (!(player instanceof ServerPlayer serverPlayer) || !canGainStrength(serverPlayer)) {
            return;
        }

        if (!state.is(StrengthTags.TRAINING_BLOCKS) || !serverPlayer.hasCorrectToolForDrops(state)) {
            return;
        }

        ItemStack heldItem = serverPlayer.getMainHandItem();

        if (!ACTION_TRACKER.allowAndRecord(
                serverPlayer.getUUID(),
                level.dimension(),
                pos,
                blockId(state.getBlock()),
                itemId(heldItem),
                level.getGameTime()
        )) {
            return;
        }

        PlayerStats.awardStrengthXp(serverPlayer, StrengthExperience.blockBreakXp());
    }

    private static InteractionResult beforeUseBlock(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer) || !canGainStrength(serverPlayer)) {
            return InteractionResult.PASS;
        }

        ItemStack stack = serverPlayer.getItemInHand(hand);

        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return InteractionResult.PASS;
        }

        BlockPlaceContext context = blockItem.updatePlacementContext(new BlockPlaceContext(serverPlayer, hand, stack, hitResult));

        if (context == null || !context.canPlace()) {
            return InteractionResult.PASS;
        }

        Block block = blockItem.getBlock();

        if (!block.defaultBlockState().is(StrengthTags.BUILDING_BLOCKS)) {
            return InteractionResult.PASS;
        }

        PENDING_PLACEMENTS.computeIfAbsent(serverPlayer.getUUID(), ignored -> new ArrayList<>()).add(
                new PendingPlacement(level.dimension(), context.getClickedPos().immutable(), block, itemId(stack), level.getGameTime())
        );

        return InteractionResult.PASS;
    }

    private static void flushPendingPlacements(MinecraftServer server) {
        Iterator<Map.Entry<UUID, List<PendingPlacement>>> mapIterator = PENDING_PLACEMENTS.entrySet().iterator();

        while (mapIterator.hasNext()) {
            Map.Entry<UUID, List<PendingPlacement>> entry = mapIterator.next();
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());

            if (player == null) {
                mapIterator.remove();
                continue;
            }

            List<PendingPlacement> placements = entry.getValue();
            Iterator<PendingPlacement> placementIterator = placements.iterator();

            while (placementIterator.hasNext()) {
                PendingPlacement placement = placementIterator.next();
                Level level = server.getLevel(placement.dimension());

                if (level == null) {
                    placementIterator.remove();
                    continue;
                }

                long age = level.getGameTime() - placement.createdTick();

                if (age > PENDING_PLACEMENT_EXPIRY_TICKS) {
                    placementIterator.remove();
                    continue;
                }

                BlockState state = level.getBlockState(placement.pos());

                if (!state.is(placement.block())) {
                    continue;
                }

                if (ACTION_TRACKER.allowAndRecord(
                        player.getUUID(),
                        placement.dimension(),
                        placement.pos(),
                        blockId(placement.block()),
                        placement.itemId(),
                        level.getGameTime()
                )) {
                    PlayerStats.awardStrengthXp(player, StrengthExperience.blockPlacementXp());
                }

                placementIterator.remove();
            }

            if (placements.isEmpty()) {
                mapIterator.remove();
            }
        }
    }

    private static boolean canGainStrength(ServerPlayer player) {
        GameType gameMode = player.gameMode.getGameModeForPlayer();
        return gameMode == GameType.SURVIVAL || gameMode == GameType.ADVENTURE;
    }

    private static ServerPlayer meleeAttacker(DamageSource source) {
        Entity sourceEntity = source.getEntity();
        Entity directEntity = source.getDirectEntity();

        if (sourceEntity instanceof ServerPlayer player && directEntity == player) {
            return player;
        }

        return null;
    }

    private static Identifier blockId(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    private static Identifier itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem());
    }

    private record PendingPlacement(ResourceKey<Level> dimension, BlockPos pos, Block block, Identifier itemId, long createdTick) {
    }
}
