package app.masterwork.simple.stats.sync;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import app.masterwork.simple.Simple;
import app.masterwork.simple.stats.ProfessionRegistry;

public record PlayerStatsSnapshotPayload(List<ProfessionSnapshot> professions) implements CustomPacketPayload {
    public static final Type<PlayerStatsSnapshotPayload> TYPE = new Type<>(Simple.id("player_stats_snapshot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerStatsSnapshotPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(ArrayList::new, ProfessionSnapshot.STREAM_CODEC),
            payload -> new ArrayList<>(payload.professions()),
            professions -> new PlayerStatsSnapshotPayload(List.copyOf(professions))
    );

    public PlayerStatsSnapshotPayload {
        professions = List.copyOf(professions);
    }

    public static PlayerStatsSnapshotPayload fromPlayer(ServerPlayer player) {
        return new PlayerStatsSnapshotPayload(
                ProfessionRegistry.all().stream()
                        .map(profession -> new ProfessionSnapshot(profession.id(), profession.get(player)))
                        .toList()
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
