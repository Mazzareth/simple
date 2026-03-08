package app.masterwork.simple.stats.strength;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import app.masterwork.simple.Simple;
import app.masterwork.simple.stats.AttributeEffectHelper;
import app.masterwork.simple.stats.PlayerStats;
import app.masterwork.simple.stats.progression.ProfessionProgress;

public final class StrengthEffects {
    private static final Identifier ATTACK_DAMAGE_MODIFIER = Identifier.parse(Simple.MOD_ID + ":strength_attack_damage");
    private static final Identifier ATTACK_KNOCKBACK_MODIFIER = Identifier.parse(Simple.MOD_ID + ":strength_attack_knockback");
    private static final Identifier MINING_EFFICIENCY_MODIFIER = Identifier.parse(Simple.MOD_ID + ":strength_mining_efficiency");

    private StrengthEffects() {
    }

    public static void apply(ServerPlayer player) {
        apply(player, PlayerStats.strengthProgress(player));
    }

    public static void apply(ServerPlayer player, ProfessionProgress progress) {
        StrengthData data = StrengthData.fromProgress(progress);

        AttributeEffectHelper.applyModifier(
                player,
                Attributes.ATTACK_DAMAGE,
                ATTACK_DAMAGE_MODIFIER,
                data.attackDamageBonus(),
                AttributeModifier.Operation.ADD_VALUE
        );
        AttributeEffectHelper.applyModifier(
                player,
                Attributes.ATTACK_KNOCKBACK,
                ATTACK_KNOCKBACK_MODIFIER,
                data.attackKnockbackBonus(),
                AttributeModifier.Operation.ADD_VALUE
        );
        AttributeEffectHelper.applyModifier(
                player,
                Attributes.MINING_EFFICIENCY,
                MINING_EFFICIENCY_MODIFIER,
                data.miningEfficiencyBonus(),
                AttributeModifier.Operation.ADD_VALUE
        );
    }
}
