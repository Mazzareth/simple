package app.masterwork.simple.stats.agility;

public final class AgilityBonuses {
    private static final double DODGE_CHANCE_PER_POINT = 0.0025D;
    private static final double MAX_DODGE_CHANCE = 0.35D;
    private static final double SPEED_BONUS_PER_POINT = 0.01D;
    private static final double MAX_SPEED_BONUS = 0.50D;

    private AgilityBonuses() {
    }

    public static double dodgeChance(int agility) {
        return clamp(agility * DODGE_CHANCE_PER_POINT, 0.0D, MAX_DODGE_CHANCE);
    }

    public static double movementSpeedMultiplier(int agility) {
        double bonus = clamp(agility * SPEED_BONUS_PER_POINT, 0.0D, MAX_SPEED_BONUS);
        return 1.0D + bonus;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
