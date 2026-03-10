package app.masterwork.simple.skills;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record SkillProfile(int bXp, Set<Identifier> unlockedSkillIds) {
    private static final Codec<SkillProfile> BASE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.optionalFieldOf("b_xp", 0).forGetter(SkillProfile::bXp),
            Identifier.CODEC.listOf()
                    .optionalFieldOf("unlocked_skills", List.of())
                    .forGetter(profile -> List.copyOf(profile.unlockedSkillIds()))
    ).apply(instance, (bXp, unlockedSkillIds) -> new SkillProfile(bXp, new LinkedHashSet<>(unlockedSkillIds))));

    public static final SkillProfile ZERO = new SkillProfile(0, Set.of());
    public static final Codec<SkillProfile> CODEC = BASE_CODEC.xmap(SkillProgression::sanitize, SkillProgression::sanitize);
    public static final StreamCodec<RegistryFriendlyByteBuf, SkillProfile> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SkillProfile::bXp,
            ByteBufCodecs.collection(ArrayList::new, Identifier.STREAM_CODEC),
            profile -> new ArrayList<>(profile.unlockedSkillIds()),
            (bXp, unlockedSkillIds) -> new SkillProfile(bXp, new LinkedHashSet<>(unlockedSkillIds))
    );

    public SkillProfile {
        bXp = Math.max(0, bXp);
        unlockedSkillIds = Set.copyOf(Objects.requireNonNull(unlockedSkillIds, "unlockedSkillIds"));
    }

    public boolean hasUnlocked(Identifier skillId) {
        return unlockedSkillIds.contains(skillId);
    }
}
