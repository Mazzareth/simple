package app.masterwork.simple.skills.sync;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record SkillSnapshot(Identifier skillId, boolean unlocked) {
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillSnapshot> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            SkillSnapshot::skillId,
            ByteBufCodecs.BOOL,
            SkillSnapshot::unlocked,
            SkillSnapshot::new
    );
}
