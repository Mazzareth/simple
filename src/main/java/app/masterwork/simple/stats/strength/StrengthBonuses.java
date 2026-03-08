package app.masterwork.simple.stats.strength;

import app.masterwork.simple.stats.progression.ProfessionProgression;

public final class StrengthBonuses {
    private static final double MAX_ATTACK_DAMAGE_BONUS = 6.0D;
    private static final double MAX_ATTACK_KNOCKBACK_BONUS = 0.75D;
    private static final double MAX_MINING_EFFICIENCY_BONUS = 2.0D;

    private StrengthBonuses() {
    }

    public static double attackDamageBonus(int level) {
        return ProfessionProgression.levelRatio(level) * MAX_ATTACK_DAMAGE_BONUS;
    }

    public static double attackKnockbackBonus(int level) {
        return ProfessionProgression.levelRatio(level) * MAX_ATTACK_KNOCKBACK_BONUS;
    }

    public static double miningEfficiencyBonus(int level) {
        return ProfessionProgression.levelRatio(level) * MAX_MINING_EFFICIENCY_BONUS;
    }
}
