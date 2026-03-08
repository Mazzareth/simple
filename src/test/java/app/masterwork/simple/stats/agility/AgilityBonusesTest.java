package app.masterwork.simple.stats.agility;

import org.junit.jupiter.api.Test;

import app.masterwork.simple.stats.progression.ProfessionProgression;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgilityBonusesTest {
    @Test
    void agilityBonusesScaleFromZeroToConfiguredCaps() {
        assertEquals(0.0D, AgilityBonuses.movementSpeedBonus(0));
        assertEquals(0.0D, AgilityBonuses.stepHeightBonus(0));
        assertEquals(0.0D, AgilityBonuses.safeFallBonus(0));

        assertEquals(0.20D, AgilityBonuses.movementSpeedBonus(ProfessionProgression.MAX_LEVEL));
        assertEquals(0.50D, AgilityBonuses.stepHeightBonus(ProfessionProgression.MAX_LEVEL));
        assertEquals(3.0D, AgilityBonuses.safeFallBonus(ProfessionProgression.MAX_LEVEL));
    }
}
