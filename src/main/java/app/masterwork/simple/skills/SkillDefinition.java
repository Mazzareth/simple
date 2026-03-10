package app.masterwork.simple.skills;

import java.util.List;
import java.util.Objects;

import net.minecraft.resources.Identifier;

public record SkillDefinition(Identifier id, SkillTier tier, int epValue, int requiredBxp, List<Identifier> prerequisites) {
    public SkillDefinition {
        id = Objects.requireNonNull(id, "id");
        tier = Objects.requireNonNull(tier, "tier");
        epValue = Math.max(0, epValue);
        requiredBxp = Math.max(0, requiredBxp);
        prerequisites = List.copyOf(Objects.requireNonNull(prerequisites, "prerequisites"));
    }

    public String nameKey() {
        return "skill." + id.getNamespace() + "." + id.getPath() + ".name";
    }

    public String descriptionKey() {
        return "skill." + id.getNamespace() + "." + id.getPath() + ".desc";
    }
}
