package app.masterwork.simple.gametest;

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

import app.masterwork.simple.stats.StatRegistry;
import app.masterwork.simple.stats.progression.ProfessionProgress;
import app.masterwork.simple.stats.progression.ProfessionProgression;

@SuppressWarnings("removal")
public final class SimpleGameTests {

    @GameTest
    public void joinRefreshAppliesModifiersOnce(GameTestHelper helper) {
        helper.runAfterDelay(1, () -> {
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            AttachmentTarget target = (AttachmentTarget) player;
            target.setAttached(StatRegistry.AGILITY.attachmentType(), new ProfessionProgress(100, 0));
            target.setAttached(StatRegistry.STRENGTH.attachmentType(), new ProfessionProgress(100, 0));

            double baseMovementSpeed = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);

            ServerPlayerEvents.JOIN.invoker().onJoin(player);
            double afterFirstJoin = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
            ServerPlayerEvents.JOIN.invoker().onJoin(player);
            double afterSecondJoin = player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);

            helper.assertTrue(afterFirstJoin > baseMovementSpeed, "expected join refresh to apply agility movement speed");
            helper.assertValueEqual(afterFirstJoin, afterSecondJoin, "join refresh should not stack modifiers");
            helper.succeed();
        });
    }

    @GameTest
    public void agilityNoLongerCancelsDamage(GameTestHelper helper) {
        helper.runAfterDelay(1, () -> {
            ServerPlayer player = helper.makeMockServerPlayerInLevel();
            StatRegistry.AGILITY.set(player, new ProfessionProgress(ProfessionProgression.MAX_LEVEL, 0));

            boolean allowed = ServerLivingEntityEvents.ALLOW_DAMAGE.invoker().allowDamage(player, helper.getLevel().damageSources().generic(), 2.0F);

            helper.assertTrue(allowed, "Agility should not cancel incoming damage");
            helper.succeed();
        });
    }

    @GameTest
    public void strengthMiningBonusOnlyHelpsProperTools(GameTestHelper helper) {
        helper.runAfterDelay(1, () -> {
            ServerPlayer baselineToolUser = helper.makeMockServerPlayerInLevel();
            ServerPlayer buffedToolUser = helper.makeMockServerPlayerInLevel();
            baselineToolUser.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
            buffedToolUser.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));

            float baselineToolSpeed = baselineToolUser.getDestroySpeed(Blocks.STONE.defaultBlockState());
            StatRegistry.STRENGTH.set(buffedToolUser, new ProfessionProgress(100, 0));
            float buffedToolSpeed = buffedToolUser.getDestroySpeed(Blocks.STONE.defaultBlockState());

            ServerPlayer baselineHandUser = helper.makeMockServerPlayerInLevel();
            ServerPlayer buffedHandUser = helper.makeMockServerPlayerInLevel();
            float baselineHandSpeed = baselineHandUser.getDestroySpeed(Blocks.STONE.defaultBlockState());
            StatRegistry.STRENGTH.set(buffedHandUser, new ProfessionProgress(100, 0));
            float buffedHandSpeed = buffedHandUser.getDestroySpeed(Blocks.STONE.defaultBlockState());

            helper.assertTrue(buffedToolSpeed > baselineToolSpeed, "strength should improve mining speed with the proper tool");
            helper.assertValueEqual(baselineHandSpeed, buffedHandSpeed, "strength should not improve bare-hand stone mining");
            helper.succeed();
        });
    }

    @GameTest
    public void thirstIsNotRegistered(GameTestHelper helper) {
        helper.assertTrue(StatRegistry.byId(Identifier.parse("simple:thirst")).isEmpty(), "Thirst should not be registered");
        helper.succeed();
    }
}
