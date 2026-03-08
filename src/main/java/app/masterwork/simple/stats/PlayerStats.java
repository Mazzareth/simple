package app.masterwork.simple.stats;

import java.util.Objects;
import java.util.function.UnaryOperator;

import net.minecraft.server.level.ServerPlayer;

import app.masterwork.simple.stats.agility.AgilityData;

/**
 * Player-facing facade for stat access.
 */
public final class PlayerStats {
    private PlayerStats() {
    }

    public static <T> T get(ServerPlayer player, IStat<T> stat) {
        Objects.requireNonNull(stat, "stat");
        return stat.get(player);
    }

    public static <T> void set(ServerPlayer player, IStat<T> stat, T value) {
        Objects.requireNonNull(stat, "stat");
        stat.set(player, value);
    }

    public static <T> T modify(ServerPlayer player, IStat<T> stat, UnaryOperator<T> modifier) {
        Objects.requireNonNull(stat, "stat");
        return stat.modify(player, modifier);
    }

    public static int agility(ServerPlayer player) {
        return StatRegistry.AGILITY.get(player);
    }

    public static void agility(ServerPlayer player, int value) {
        StatRegistry.AGILITY.set(player, value);
    }

    public static AgilityData agilityData(ServerPlayer player) {
        return AgilityData.fromPlayer(player);
    }

    public static int strength(ServerPlayer player) {
        return StatRegistry.STRENGTH.get(player);
    }

    public static void strength(ServerPlayer player, int value) {
        StatRegistry.STRENGTH.set(player, value);
    }

    public static int thirst(ServerPlayer player) {
        return StatRegistry.THIRST.get(player);
    }

    public static void thirst(ServerPlayer player, int value) {
        StatRegistry.THIRST.set(player, value);
    }
}
