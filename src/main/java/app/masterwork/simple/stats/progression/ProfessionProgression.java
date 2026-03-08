package app.masterwork.simple.stats.progression;

public final class ProfessionProgression {
    public static final int MAX_LEVEL = 100;
    private static final int BASE_XP_TO_NEXT_LEVEL = 100;
    private static final int XP_PER_LEVEL = 25;

    private ProfessionProgression() {
    }

    public static ProfessionProgress sanitize(ProfessionProgress progress) {
        if (progress == null) {
            return ProfessionProgress.ZERO;
        }

        int level = clamp(progress.level(), 0, MAX_LEVEL);
        int xp = progress.xp();

        if (level >= MAX_LEVEL) {
            return new ProfessionProgress(MAX_LEVEL, 0);
        }

        int xpCap = Math.max(0, xpToNextLevel(level) - 1);
        return new ProfessionProgress(level, clamp(xp, 0, xpCap));
    }

    public static int xpToNextLevel(int level) {
        int clampedLevel = clamp(level, 0, MAX_LEVEL);

        if (clampedLevel >= MAX_LEVEL) {
            return 0;
        }

        return BASE_XP_TO_NEXT_LEVEL + (XP_PER_LEVEL * clampedLevel);
    }

    public static ProfessionProgress grantXp(ProfessionProgress progress, int amount) {
        ProfessionProgress sanitized = sanitize(progress);

        if (amount <= 0 || sanitized.level() >= MAX_LEVEL) {
            return sanitized;
        }

        int level = sanitized.level();
        int xp = sanitized.xp() + amount;

        while (level < MAX_LEVEL) {
            int xpToNextLevel = xpToNextLevel(level);

            if (xp < xpToNextLevel) {
                break;
            }

            xp -= xpToNextLevel;
            level++;

            if (level >= MAX_LEVEL) {
                return new ProfessionProgress(MAX_LEVEL, 0);
            }
        }

        return sanitize(new ProfessionProgress(level, xp));
    }

    public static double levelRatio(int level) {
        return clamp(level, 0, MAX_LEVEL) / (double) MAX_LEVEL;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
