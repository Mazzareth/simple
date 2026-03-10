package app.masterwork.simple.skills.sync;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class PlayerSkillsNetworking {
    private static boolean registered;

    private PlayerSkillsNetworking() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        PayloadTypeRegistry.serverboundPlay().register(RequestPlayerSkillsPayload.TYPE, RequestPlayerSkillsPayload.STREAM_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(PlayerSkillsSnapshotPayload.TYPE, PlayerSkillsSnapshotPayload.STREAM_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(RequestPlayerSkillsPayload.TYPE, (payload, context) ->
                ServerPlayNetworking.send(context.player(), PlayerSkillsSnapshotPayload.fromPlayer(context.player()))
        );
    }
}
