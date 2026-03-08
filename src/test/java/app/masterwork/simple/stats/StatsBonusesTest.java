package app.masterwork.simple.stats;

import org.junit.jupiter.api.Test;

import app.masterwork.simple.stats.agility.AgilityData;
import app.masterwork.simple.stats.progression.ProfessionProgress;
import app.masterwork.simple.stats.strength.StrengthData;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatsBonusesTest {
    @Test
    void agilityBonusesScaleToConfiguredMaximums() {
        AgilityData data = AgilityData.fromProgress(new ProfessionProgress(100, 0));

        assertEquals(0.20D, data.movementSpeedBonus());
        assertEquals(0.50D, data.stepHeightBonus());
        assertEquals(3.0D, data.safeFallBonus());
    }

    @Test
    void strengthBonusesScaleToConfiguredMaximums() {
        StrengthData data = StrengthData.fromProgress(new ProfessionProgress(100, 0));

        assertEquals(6.0D, data.attackDamageBonus());
        assertEquals(0.75D, data.attackKnockbackBonus());
        assertEquals(2.0D, data.miningEfficiencyBonus());
    }
}
