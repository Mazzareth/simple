package app.masterwork.simple.stats.agility;

import net.minecraft.server.level.ServerPlayer;

import app.masterwork.simple.stats.PlayerStats;
import app.masterwork.simple.stats.progression.ProfessionProgress;

public record AgilityData(ProfessionProgress progress, double movementSpeedBonus, double stepHeightBonus, double safeFallBonus) {
    public static AgilityData fromPlayer(ServerPlayer player) {
        return fromProgress(PlayerStats.agilityProgress(player));
    }

    public static AgilityData fromProgress(ProfessionProgress progress) {
        return new AgilityData(
                progress,
                AgilityBonuses.movementSpeedBonus(progress.level()),
                AgilityBonuses.stepHeightBonus(progress.level()),
                AgilityBonuses.safeFallBonus(progress.level())
        );
    }
}
