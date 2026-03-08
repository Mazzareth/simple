package app.masterwork.simple.stats.agility;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import app.masterwork.simple.stats.PlayerStats;

public final class AgilityEvents {
    private static final int SAMPLE_INTERVAL_TICKS = 20;
    private static final Map<UUID, TrackerState> TRACKER_STATES = new HashMap<>();
    private static boolean registered;

    private AgilityEvents() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;
        ServerPlayerEvents.JOIN.register(player -> TRACKER_STATES.put(player.getUUID(), TrackerState.create(player)));
        ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> TRACKER_STATES.put(newPlayer.getUUID(), TrackerState.create(newPlayer)));
        ServerPlayerEvents.LEAVE.register(player -> TRACKER_STATES.remove(player.getUUID()));
        ServerTickEvents.END_SERVER_TICK.register(AgilityEvents::samplePlayers);
    }

    private static void samplePlayers(MinecraftServer server) {
        if (server.getTickCount() % SAMPLE_INTERVAL_TICKS != 0) {
            return;
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            samplePlayer(player);
        }
    }

    private static void samplePlayer(ServerPlayer player) {
        AgilityExperience.MovementSnapshot current = AgilityExperience.sample(player);
        TrackerState previous = TRACKER_STATES.get(player.getUUID());

        if (previous == null) {
            TRACKER_STATES.put(player.getUUID(), new TrackerState(current, 0));
            return;
        }

        if (!canGainAgility(player)) {
            TRACKER_STATES.put(player.getUUID(), new TrackerState(current, previous.carryScore()));
            return;
        }

        AgilityExperience.SampleResult sample = AgilityExperience.awardFromDelta(previous.snapshot(), current, previous.carryScore());

        if (sample.xpAwarded() > 0) {
            PlayerStats.awardAgilityXp(player, sample.xpAwarded());
        }

        TRACKER_STATES.put(player.getUUID(), new TrackerState(sample.snapshot(), sample.carryScore()));
    }

    private static boolean canGainAgility(ServerPlayer player) {
        return !player.isCreative() && !player.isSpectator() && !player.isPassenger();
    }

    private record TrackerState(AgilityExperience.MovementSnapshot snapshot, int carryScore) {
        private static TrackerState create(ServerPlayer player) {
            return new TrackerState(AgilityExperience.sample(player), 0);
        }
    }
}
