package app.masterwork.simple.stats.progression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProfessionProgressionTest {
    @Test
    void grantXpLevelsUpAndCarriesRemainder() {
        ProfessionProgress progress = ProfessionProgression.grantXp(ProfessionProgress.ZERO, 140);

        assertEquals(1, progress.level());
        assertEquals(40, progress.xp());
    }

    @Test
    void grantXpCapsAtMaxLevel() {
        ProfessionProgress progress = ProfessionProgression.grantXp(new ProfessionProgress(99, 120), 10_000);

        assertEquals(ProfessionProgression.MAX_LEVEL, progress.level());
        assertEquals(0, progress.xp());
    }

    @Test
    void sanitizeClampsInvalidValues() {
        assertEquals(ProfessionProgress.ZERO, ProfessionProgression.sanitize(new ProfessionProgress(-5, -10)));
        assertEquals(new ProfessionProgress(ProfessionProgression.MAX_LEVEL, 0), ProfessionProgression.sanitize(new ProfessionProgress(250, 5000)));
    }
}
