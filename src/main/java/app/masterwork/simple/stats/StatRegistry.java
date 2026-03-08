package app.masterwork.simple.stats;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import net.minecraft.resources.Identifier;

import app.masterwork.simple.stats.agility.AgilityStat;
import app.masterwork.simple.stats.strength.StrengthStat;

/**
 * Compatibility facade for the legacy stat entry points.
 */
public final class StatRegistry {
    public static final AgilityStat AGILITY = ProfessionRegistry.AGILITY;
    public static final StrengthStat STRENGTH = ProfessionRegistry.STRENGTH;

    private StatRegistry() {
    }

    public static void bootstrap() {
        ProfessionRegistry.bootstrap();
    }

    public static Collection<IStat<?>> all() {
        return List.of((IStat<?>) AGILITY, STRENGTH);
    }

    public static Optional<IStat<?>> byId(Identifier id) {
        if (AGILITY.id().equals(id)) {
            return Optional.of(AGILITY);
        }

        if (STRENGTH.id().equals(id)) {
            return Optional.of(STRENGTH);
        }

        return Optional.empty();
    }
}
