package app.masterwork.simple.stats;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.resources.Identifier;

import app.masterwork.simple.stats.agility.AgilityStat;
import app.masterwork.simple.stats.impl.ThirstStat;
import app.masterwork.simple.stats.strength.StrengthStat;

/**
 * Central registration and lookup point for all stats.
 */
public final class StatRegistry {
    private static final Map<Identifier, IStat<?>> STATS = new LinkedHashMap<>();

    public static final AgilityStat AGILITY = register(new AgilityStat());
    public static final StrengthStat STRENGTH = register(new StrengthStat());
    public static final ThirstStat THIRST = register(new ThirstStat());

    private StatRegistry() {
    }

    public static void bootstrap() {
        // Deliberately empty. Accessing this class triggers static registration.
    }

    public static Collection<IStat<?>> all() {
        return Collections.unmodifiableCollection(STATS.values());
    }

    public static Optional<IStat<?>> byId(Identifier id) {
        return Optional.ofNullable(STATS.get(id));
    }

    private static <T, S extends IStat<T>> S register(S stat) {
        IStat<?> previous = STATS.putIfAbsent(stat.id(), stat);

        if (previous != null) {
            throw new IllegalStateException("Duplicate stat id: " + stat.id());
        }

        return stat;
    }
}
