package app.masterwork.simple.stats.sync;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import app.masterwork.simple.stats.progression.ProfessionProgress;

public record ProfessionSnapshot(Identifier professionId, ProfessionProgress progress) {
    public static final StreamCodec<RegistryFriendlyByteBuf, ProfessionSnapshot> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC,
            ProfessionSnapshot::professionId,
            ProfessionProgress.STREAM_CODEC,
            ProfessionSnapshot::progress,
            ProfessionSnapshot::new
    );
}
