package app.masterwork.simple.stats.xp;

import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

public record XpRewardTarget(XpRewardTargetType type, Identifier id) {
    public static final Codec<XpRewardTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            XpRewardTargetType.CODEC.fieldOf("type").forGetter(XpRewardTarget::type),
            Identifier.CODEC.fieldOf("id").forGetter(XpRewardTarget::id)
    ).apply(instance, XpRewardTarget::new));

    public XpRewardTarget {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(id, "id");
    }

    public boolean exactMatch() {
        return type.exactMatch();
    }
}
