package app.masterwork.simple.stats.xp;

import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record XpRewardEntry(XpRewardTarget match, int xp, int priority) {
    private static final Codec<Integer> POSITIVE_XP_CODEC = Codec.INT.validate(value ->
            value > 0 ? DataResult.success(value) : DataResult.error(() -> "xp must be positive"));

    public static final Codec<XpRewardEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            XpRewardTarget.CODEC.fieldOf("match").forGetter(XpRewardEntry::match),
            POSITIVE_XP_CODEC.fieldOf("xp").forGetter(XpRewardEntry::xp),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(XpRewardEntry::priority)
    ).apply(instance, XpRewardEntry::new));

    public XpRewardEntry {
        Objects.requireNonNull(match, "match");
    }
}
