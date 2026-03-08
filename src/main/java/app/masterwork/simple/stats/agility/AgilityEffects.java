package app.masterwork.simple.stats.agility;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import app.masterwork.simple.Simple;
import app.masterwork.simple.stats.AttributeEffectHelper;
import app.masterwork.simple.stats.PlayerStats;
import app.masterwork.simple.stats.progression.ProfessionProgress;

public final class AgilityEffects {
    private static final Identifier MOVEMENT_SPEED_MODIFIER = Identifier.parse(Simple.MOD_ID + ":agility_movement_speed");
    private static final Identifier STEP_HEIGHT_MODIFIER = Identifier.parse(Simple.MOD_ID + ":agility_step_height");
    private static final Identifier SAFE_FALL_MODIFIER = Identifier.parse(Simple.MOD_ID + ":agility_safe_fall");

    private AgilityEffects() {
    }

    public static void apply(ServerPlayer player) {
        apply(player, PlayerStats.agilityProgress(player));
    }

    public static void apply(ServerPlayer player, ProfessionProgress progress) {
        AgilityData data = AgilityData.fromProgress(progress);

        AttributeEffectHelper.applyModifier(
                player,
                Attributes.MOVEMENT_SPEED,
                MOVEMENT_SPEED_MODIFIER,
                data.movementSpeedBonus(),
                AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
        );
        AttributeEffectHelper.applyModifier(
                player,
                Attributes.STEP_HEIGHT,
                STEP_HEIGHT_MODIFIER,
                data.stepHeightBonus(),
                AttributeModifier.Operation.ADD_VALUE
        );
        AttributeEffectHelper.applyModifier(
                player,
                Attributes.SAFE_FALL_DISTANCE,
                SAFE_FALL_MODIFIER,
                data.safeFallBonus(),
                AttributeModifier.Operation.ADD_VALUE
        );
    }
}
