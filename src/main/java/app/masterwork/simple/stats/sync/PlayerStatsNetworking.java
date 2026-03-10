package app.masterwork.simple.stats.sync;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class PlayerStatsNetworking {
    private static boolean registered;

    private PlayerStatsNetworking() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        PayloadTypeRegistry.serverboundPlay().register(RequestPlayerStatsPayload.TYPE, RequestPlayerStatsPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PlayerStatsSnapshotPayload.TYPE, PlayerStatsSnapshotPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestPlayerStatsPayload.TYPE, (payload, context) ->
                ServerPlayNetworking.send(context.player(), PlayerStatsSnapshotPayload.fromPlayer(context.player()))
        );
    }
}
