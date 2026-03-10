package app.masterwork.simple.skills;

import java.util.Set;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SkillProgressionTest {
    @Test
    void awardBxpUnlocksSkillsWhenThresholdsAndPrerequisitesMatch() {
        SkillProfile profile = SkillProgression.awardBxp(SkillProfile.ZERO, 1_000);

        assertEquals(1_000, profile.bXp());
        assertEquals(8, SkillProgression.unlockedCount(profile));
        assertTrue(profile.hasUnlocked(SkillRegistry.GREAT_SAGE.id()));
        assertEquals(5_600, SkillProgression.ep(profile));
    }

    @Test
    void awardBxpStopsBeforeLockedThresholds() {
        SkillProfile profile = SkillProgression.awardBxp(SkillProfile.ZERO, 349);

        assertEquals(349, profile.bXp());
        assertEquals(4, SkillProgression.unlockedCount(profile));
        assertFalse(profile.hasUnlocked(SkillRegistry.MAGIC_SENSE.id()));
        assertEquals(949, SkillProgression.ep(profile));
    }

    @Test
    void sanitizeClampsBxpAndDropsUnknownSkills() {
        SkillProfile sanitized = SkillProgression.sanitize(new SkillProfile(-25, Set.of(
                SkillRegistry.HEAT_RESISTANCE.id(),
                Identifier.parse("simple:missing_skill")
        )));

        assertEquals(0, sanitized.bXp());
        assertEquals(Set.of(SkillRegistry.HEAT_RESISTANCE.id()), sanitized.unlockedSkillIds());
    }

    @Test
    void unlockRejectsUnknownIds() {
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> SkillProgression.unlock(SkillProfile.ZERO, Identifier.parse("simple:not_real"))
        );
    }
}
