package app.masterwork.simple.stats.agility;

import net.minecraft.server.level.ServerPlayer;

import app.masterwork.simple.stats.PlayerStats;

public record AgilityData(int value, double movementSpeedMultiplier, double dodgeChance) {
    public static AgilityData fromPlayer(ServerPlayer player) {
        return fromValue(PlayerStats.agility(player));
    }

    public static AgilityData fromValue(int value) {
        return new AgilityData(
                value,
                AgilityBonuses.movementSpeedMultiplier(value),
                AgilityBonuses.dodgeChance(value)
        );
    }
}
