package app.masterwork.simple.skills;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.resources.Identifier;

import app.masterwork.simple.Simple;

public final class SkillRegistry {
    private static final Map<Identifier, SkillDefinition> SKILLS = new LinkedHashMap<>();

    public static final SkillDefinition HEAT_RESISTANCE = register(common("heat_resistance", 50));
    public static final SkillDefinition COLD_RESISTANCE = register(common("cold_resistance", 100));
    public static final SkillDefinition POISON_RESISTANCE = register(common("poison_resistance", 150));
    public static final SkillDefinition PAIN_NULLIFICATION = register(common("pain_nullification", 225));
    public static final SkillDefinition MAGIC_SENSE = register(extra("magic_sense", 350, COLD_RESISTANCE.id()));
    public static final SkillDefinition STEEL_BODY = register(extra("steel_body", 500, PAIN_NULLIFICATION.id()));
    public static final SkillDefinition SELF_REGENERATION = register(extra("self_regeneration", 650, POISON_RESISTANCE.id()));
    public static final SkillDefinition GREAT_SAGE = register(
            unique(
                    "great_sage",
                    1_000,
                    MAGIC_SENSE.id(),
                    STEEL_BODY.id(),
                    SELF_REGENERATION.id()
            )
    );

    private SkillRegistry() {
    }

    public static void bootstrap() {
        // Accessing this class triggers static registration.
    }

    public static Collection<SkillDefinition> all() {
        return Collections.unmodifiableCollection(SKILLS.values());
    }

    public static Optional<SkillDefinition> byId(Identifier id) {
        return Optional.ofNullable(SKILLS.get(id));
    }

    public static boolean contains(Identifier id) {
        return SKILLS.containsKey(id);
    }

    public static int totalCount() {
        return SKILLS.size();
    }

    private static SkillDefinition common(String path, int requiredBxp, Identifier... prerequisites) {
        return skill(path, SkillTier.COMMON, requiredBxp, prerequisites);
    }

    private static SkillDefinition extra(String path, int requiredBxp, Identifier... prerequisites) {
        return skill(path, SkillTier.EXTRA, requiredBxp, prerequisites);
    }

    private static SkillDefinition unique(String path, int requiredBxp, Identifier... prerequisites) {
        return skill(path, SkillTier.UNIQUE, requiredBxp, prerequisites);
    }

    private static SkillDefinition skill(String path, SkillTier tier, int requiredBxp, Identifier... prerequisites) {
        return new SkillDefinition(Simple.id(path), tier, tier.epValue(), requiredBxp, List.of(prerequisites));
    }

    private static SkillDefinition register(SkillDefinition definition) {
        SkillDefinition previous = SKILLS.putIfAbsent(definition.id(), definition);

        if (previous != null) {
            throw new IllegalStateException("Duplicate skill id: " + definition.id());
        }

        return definition;
    }
}
