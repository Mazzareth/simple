package app.masterwork.simple.stats.sync;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import app.masterwork.simple.Simple;

public record RequestPlayerStatsPayload() implements CustomPacketPayload {
    public static final Type<RequestPlayerStatsPayload> TYPE = new Type<>(Simple.id("request_player_stats"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestPlayerStatsPayload> STREAM_CODEC = StreamCodec.unit(new RequestPlayerStatsPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
