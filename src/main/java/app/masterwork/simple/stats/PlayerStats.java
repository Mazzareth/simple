package app.masterwork.simple.stats;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;

import app.masterwork.simple.stats.agility.AgilityData;
import app.masterwork.simple.stats.progression.ProfessionProgress;
import app.masterwork.simple.stats.progression.ProfessionProgression;
import app.masterwork.simple.stats.progression.ProfessionStat;
import app.masterwork.simple.stats.strength.StrengthData;

/**
 * Player-facing facade for stat progression and derived effects.
 */
public final class PlayerStats {
    private PlayerStats() {
    }

    public static ProfessionProgress getProgress(ServerPlayer player, Identifier professionId) {
        return profession(professionId).get(player);
    }

    public static ProfessionProgress progress(ServerPlayer player, Identifier professionId) {
        return getProgress(player, professionId);
    }

    public static ProfessionProgress awardXp(ServerPlayer player, Identifier professionId, int amount) {
        ProfessionStat profession = profession(professionId);

        if (amount <= 0) {
            return profession.get(player);
        }

        return profession.modify(player, progress -> ProfessionProgression.grantXp(progress, amount));
    }

    public static ProfessionStat profession(Identifier professionId) {
        return ProfessionRegistry.byId(professionId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown profession id: " + professionId));
    }

    public static ProfessionProgress agilityProgress(ServerPlayer player) {
        return getProgress(player, ProfessionRegistry.AGILITY.id());
    }

    public static ProfessionProgress awardAgilityXp(ServerPlayer player, int amount) {
        return awardXp(player, ProfessionRegistry.AGILITY.id(), amount);
    }

    public static AgilityData agilityData(ServerPlayer player) {
        return AgilityData.fromPlayer(player);
    }

    public static ProfessionProgress strengthProgress(ServerPlayer player) {
        return getProgress(player, ProfessionRegistry.STRENGTH.id());
    }

    public static ProfessionProgress awardStrengthXp(ServerPlayer player, int amount) {
        return awardXp(player, ProfessionRegistry.STRENGTH.id(), amount);
    }

    public static StrengthData strengthData(ServerPlayer player) {
        return StrengthData.fromPlayer(player);
    }
}
