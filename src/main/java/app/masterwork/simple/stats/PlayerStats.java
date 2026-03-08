package app.masterwork.simple.stats;

import net.minecraft.server.level.ServerPlayer;

import app.masterwork.simple.stats.agility.AgilityData;
import app.masterwork.simple.stats.progression.ProfessionProgress;
import app.masterwork.simple.stats.progression.ProfessionProgression;
import app.masterwork.simple.stats.strength.StrengthData;

/**
 * Player-facing facade for stat progression and derived effects.
 */
public final class PlayerStats {
    private PlayerStats() {
    }

    public static ProfessionProgress agilityProgress(ServerPlayer player) {
        return StatRegistry.AGILITY.get(player);
    }

    public static ProfessionProgress awardAgilityXp(ServerPlayer player, int amount) {
        if (amount <= 0) {
            return agilityProgress(player);
        }

        return StatRegistry.AGILITY.modify(player, progress -> ProfessionProgression.grantXp(progress, amount));
    }

    public static AgilityData agilityData(ServerPlayer player) {
        return AgilityData.fromPlayer(player);
    }

    public static ProfessionProgress strengthProgress(ServerPlayer player) {
        return StatRegistry.STRENGTH.get(player);
    }

    public static ProfessionProgress awardStrengthXp(ServerPlayer player, int amount) {
        if (amount <= 0) {
            return strengthProgress(player);
        }

        return StatRegistry.STRENGTH.modify(player, progress -> ProfessionProgression.grantXp(progress, amount));
    }

    public static StrengthData strengthData(ServerPlayer player) {
        return StrengthData.fromPlayer(player);
    }
}
