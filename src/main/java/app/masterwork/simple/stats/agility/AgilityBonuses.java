package app.masterwork.simple.stats.agility;

import app.masterwork.simple.stats.progression.ProfessionProgression;

public final class AgilityBonuses {
    private static final double MAX_MOVEMENT_SPEED_BONUS = 0.20D;
    private static final double MAX_STEP_HEIGHT_BONUS = 0.50D;
    private static final double MAX_SAFE_FALL_BONUS = 3.0D;

    private AgilityBonuses() {
    }

    public static double movementSpeedBonus(int level) {
        return ProfessionProgression.levelRatio(level) * MAX_MOVEMENT_SPEED_BONUS;
    }

    public static double stepHeightBonus(int level) {
        return ProfessionProgression.levelRatio(level) * MAX_STEP_HEIGHT_BONUS;
    }

    public static double safeFallBonus(int level) {
        return ProfessionProgression.levelRatio(level) * MAX_SAFE_FALL_BONUS;
    }
}
