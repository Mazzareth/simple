package app.masterwork.simple.client.stats;

import java.util.LinkedHashMap;
import java.util.Map;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.resources.Identifier;

import app.masterwork.simple.stats.progression.ProfessionProgress;
import app.masterwork.simple.stats.sync.PlayerStatsSnapshotPayload;
import app.masterwork.simple.stats.sync.ProfessionSnapshot;
import app.masterwork.simple.stats.sync.RequestPlayerStatsPayload;

public final class ClientPlayerStats {
    private static Map<Identifier, ProfessionProgress> progressByProfession = Map.of();
    private static boolean loading;
    private static boolean registered;

    private ClientPlayerStats() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        ClientPlayNetworking.registerGlobalReceiver(PlayerStatsSnapshotPayload.TYPE, (payload, context) -> accept(payload));
        ClientPlayConnectionEvents.DISCONNECT.register((listener, client) -> clear());
    }

    public static void requestRefresh() {
        loading = true;

        if (ClientPlayNetworking.canSend(RequestPlayerStatsPayload.TYPE)) {
            ClientPlayNetworking.send(new RequestPlayerStatsPayload());
            return;
        }

        loading = false;
    }

    public static Map<Identifier, ProfessionProgress> snapshot() {
        return progressByProfession;
    }

    public static boolean isLoading() {
        return loading;
    }

    public static void clear() {
        progressByProfession = Map.of();
        loading = false;
    }

    private static void accept(PlayerStatsSnapshotPayload payload) {
        LinkedHashMap<Identifier, ProfessionProgress> next = new LinkedHashMap<>();

        for (ProfessionSnapshot profession : payload.professions()) {
            next.put(profession.professionId(), profession.progress());
        }

        progressByProfession = Map.copyOf(next);
        loading = false;
    }
}
