package app.masterwork.simple.stats.progression;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ProfessionProgress(int level, int xp) {
    private static final Codec<ProfessionProgress> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("level", 0).forGetter(ProfessionProgress::level),
            Codec.INT.optionalFieldOf("xp", 0).forGetter(ProfessionProgress::xp)
    ).apply(instance, ProfessionProgress::new));

    public static final ProfessionProgress ZERO = new ProfessionProgress(0, 0);
    public static final Codec<ProfessionProgress> CODEC = BASE_CODEC.xmap(
            ProfessionProgression::sanitize,
            ProfessionProgression::sanitize
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, ProfessionProgress> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            ProfessionProgress::level,
            ByteBufCodecs.VAR_INT,
            ProfessionProgress::xp,
            ProfessionProgress::new
    );
}
