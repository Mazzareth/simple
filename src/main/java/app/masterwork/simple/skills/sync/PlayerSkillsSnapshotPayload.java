package app.masterwork.simple.skills.sync;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import app.masterwork.simple.Simple;
import app.masterwork.simple.skills.PlayerSkills;
import app.masterwork.simple.skills.SkillProfile;

public record PlayerSkillsSnapshotPayload(int bXp, int ep, List<SkillSnapshot> skills) implements CustomPacketPayload {
    public static final Type<PlayerSkillsSnapshotPayload> TYPE = new Type<>(Simple.id("player_skills_snapshot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerSkillsSnapshotPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            PlayerSkillsSnapshotPayload::bXp,
            ByteBufCodecs.VAR_INT,
            PlayerSkillsSnapshotPayload::ep,
            ByteBufCodecs.collection(ArrayList::new, SkillSnapshot.STREAM_CODEC),
            payload -> new ArrayList<>(payload.skills()),
            (bXp, ep, skills) -> new PlayerSkillsSnapshotPayload(bXp, ep, skills)
    );

    public PlayerSkillsSnapshotPayload {
        bXp = Math.max(0, bXp);
        ep = Math.max(0, ep);
        skills = List.copyOf(skills);
    }

    public static PlayerSkillsSnapshotPayload fromPlayer(ServerPlayer player) {
        SkillProfile profile = PlayerSkills.profile(player);
        return new PlayerSkillsSnapshotPayload(profile.bXp(), PlayerSkills.ep(player), PlayerSkills.snapshots(player));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
