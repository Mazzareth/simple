package app.masterwork.simple.stats.strength;

public final class StrengthExperience {
    private static final int MIN_DAMAGE_XP = 1;
    private static final int KILL_XP = 3;
    private static final int BLOCK_PLACE_XP = 1;

    private StrengthExperience() {
    }

    public static int meleeDamageXp(float damageTaken) {
        return Math.max(MIN_DAMAGE_XP, (int) Math.floor(damageTaken));
    }

    public static int meleeKillXp() {
        return KILL_XP;
    }

    public static int blockPlacementXp() {
        return BLOCK_PLACE_XP;
    }
}
