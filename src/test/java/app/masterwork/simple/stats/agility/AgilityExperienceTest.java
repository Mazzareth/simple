package app.masterwork.simple.stats.agility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AgilityExperienceTest {
    @Test
    void awardsXpFromWeightedMovementDelta() {
        AgilityExperience.MovementSnapshot previous = new AgilityExperience.MovementSnapshot(0, 0, 0, 0);
        AgilityExperience.MovementSnapshot current = new AgilityExperience.MovementSnapshot(500, 250, 0, 0);

        AgilityExperience.SampleResult result = AgilityExperience.awardFromDelta(previous, current, 0);

        assertEquals(1, result.xpAwarded());
        assertEquals(0, result.carryScore());
        assertEquals(current, result.snapshot());
    }

    @Test
    void preservesCarryScoreBetweenSamples() {
        AgilityExperience.MovementSnapshot previous = new AgilityExperience.MovementSnapshot(0, 0, 0, 0);
        AgilityExperience.MovementSnapshot current = new AgilityExperience.MovementSnapshot(200, 100, 0, 0);

        AgilityExperience.SampleResult result = AgilityExperience.awardFromDelta(previous, current, 1_400);

        assertEquals(1, result.xpAwarded());
        assertEquals(200, result.carryScore());
    }
}
