package app.masterwork.simple.skills.sync;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import app.masterwork.simple.Simple;

public record RequestPlayerSkillsPayload() implements CustomPacketPayload {
    public static final Type<RequestPlayerSkillsPayload> TYPE = new Type<>(Simple.id("request_player_skills"));
    public static final StreamCodec<RegistryFriendlyByteBuf, RequestPlayerSkillsPayload> STREAM_CODEC = StreamCodec.unit(new RequestPlayerSkillsPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
