package app.masterwork.simple.skills;

import java.util.LinkedHashSet;

import net.minecraft.resources.Identifier;

public final class SkillProgression {
    private SkillProgression() {
    }

    public static SkillProfile sanitize(SkillProfile profile) {
        int sanitizedBxp = Math.max(0, profile.bXp());
        LinkedHashSet<Identifier> sanitizedUnlocks = new LinkedHashSet<>();

        for (SkillDefinition definition : SkillRegistry.all()) {
            if (profile.unlockedSkillIds().contains(definition.id())) {
                sanitizedUnlocks.add(definition.id());
            }
        }

        return new SkillProfile(sanitizedBxp, sanitizedUnlocks);
    }

    public static SkillProfile awardBxp(SkillProfile profile, int amount) {
        if (amount <= 0) {
            return sanitize(profile);
        }

        return unlockEligible(new SkillProfile(profile.bXp() + amount, profile.unlockedSkillIds()));
    }

    public static SkillProfile unlock(SkillProfile profile, Identifier skillId) {
        SkillRegistry.byId(skillId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown skill id: " + skillId));

        LinkedHashSet<Identifier> unlocked = new LinkedHashSet<>(profile.unlockedSkillIds());
        unlocked.add(skillId);
        return sanitize(new SkillProfile(profile.bXp(), unlocked));
    }

    public static boolean canUnlock(SkillProfile profile, SkillDefinition definition) {
        if (profile.bXp() < definition.requiredBxp() || profile.hasUnlocked(definition.id())) {
            return false;
        }

        for (Identifier prerequisite : definition.prerequisites()) {
            if (!profile.hasUnlocked(prerequisite)) {
                return false;
            }
        }

        return true;
    }

    public static int ep(SkillProfile profile) {
        int ep = Math.max(0, profile.bXp());

        for (Identifier skillId : profile.unlockedSkillIds()) {
            ep += SkillRegistry.byId(skillId)
                    .map(SkillDefinition::epValue)
                    .orElse(0);
        }

        return ep;
    }

    public static int unlockedCount(SkillProfile profile) {
        return profile.unlockedSkillIds().size();
    }

    private static SkillProfile unlockEligible(SkillProfile profile) {
        SkillProfile next = sanitize(profile);
        boolean changed;

        do {
            changed = false;

            for (SkillDefinition definition : SkillRegistry.all()) {
                if (canUnlock(next, definition)) {
                    next = unlock(next, definition.id());
                    changed = true;
                }
            }
        } while (changed);

        return next;
    }
}
