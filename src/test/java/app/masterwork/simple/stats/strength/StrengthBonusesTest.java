package app.masterwork.simple.stats.strength;

import org.junit.jupiter.api.Test;

import app.masterwork.simple.stats.progression.ProfessionProgression;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StrengthBonusesTest {
    @Test
    void strengthBonusesScaleFromZeroToConfiguredCaps() {
        assertEquals(0.0D, StrengthBonuses.attackDamageBonus(0));
        assertEquals(0.0D, StrengthBonuses.attackKnockbackBonus(0));
        assertEquals(0.0D, StrengthBonuses.miningEfficiencyBonus(0));

        assertEquals(6.0D, StrengthBonuses.attackDamageBonus(ProfessionProgression.MAX_LEVEL));
        assertEquals(0.75D, StrengthBonuses.attackKnockbackBonus(ProfessionProgression.MAX_LEVEL));
        assertEquals(2.0D, StrengthBonuses.miningEfficiencyBonus(ProfessionProgression.MAX_LEVEL));
    }
}
