package app.masterwork.simple.stats;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.server.level.ServerPlayer;

import app.masterwork.simple.stats.agility.AgilityEffects;
import app.masterwork.simple.stats.strength.StrengthEffects;

public final class PlayerStatEffects {
    private static boolean registered;

    private PlayerStatEffects() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        ServerPlayerEvents.JOIN.register(PlayerStatEffects::refreshAll);
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> refreshAll(newPlayer));
    }

    public static void refreshAll(ServerPlayer player) {
        AgilityEffects.apply(player);
        StrengthEffects.apply(player);
    }
}
