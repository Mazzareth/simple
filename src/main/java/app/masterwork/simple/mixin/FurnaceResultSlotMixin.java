package app.masterwork.simple.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.item.ItemStack;

import app.masterwork.simple.stats.progression.ProfessionXpRules;
import app.masterwork.simple.stats.xp.XpRewards;

@Mixin(FurnaceResultSlot.class)
abstract class FurnaceResultSlotMixin {
    @Shadow
    @Final
    private Player player;

    @Shadow
    private int removeCount;

    @Inject(
            method = "checkTakeAchievements",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;awardUsedRecipesAndPopExperience(Lnet/minecraft/server/level/ServerPlayer;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void simple$awardProfessionXp(ItemStack stack, CallbackInfo ci) {
        Object container = ((SlotAccessor) this).simple$getContainer();

        if (!(player instanceof ServerPlayer serverPlayer)
                || !(container instanceof net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity)
                || !ProfessionXpRules.canGainGameplayXp(serverPlayer)
                || removeCount <= 0
                || stack.isEmpty()
                || !XpRewards.hasSmeltingRewards(stack)) {
            return;
        }

        XpRewards.awardSmelting(serverPlayer, stack, removeCount);
    }
}
