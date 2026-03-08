package app.masterwork.simple.stats;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import net.minecraft.resources.Identifier;

import app.masterwork.simple.Simple;
import app.masterwork.simple.stats.agility.AgilityStat;
import app.masterwork.simple.stats.progression.PassiveProfessionStat;
import app.masterwork.simple.stats.progression.ProfessionStat;
import app.masterwork.simple.stats.strength.StrengthStat;

/**
 * Generic registry for all attachment-backed profession tracks.
 */
public final class ProfessionRegistry {
    private static final Map<Identifier, ProfessionStat> PROFESSIONS = new LinkedHashMap<>();

    public static final AgilityStat AGILITY = register(new AgilityStat());
    public static final StrengthStat STRENGTH = register(new StrengthStat());
    public static final ProfessionStat QUARRYING = register(new PassiveProfessionStat(Simple.id("quarrying")));
    public static final ProfessionStat MINING = register(new PassiveProfessionStat(Simple.id("mining")));
    public static final ProfessionStat WOODCUTTING = register(new PassiveProfessionStat(Simple.id("woodcutting")));

    private ProfessionRegistry() {
    }

    public static void bootstrap() {
        // Deliberately empty. Accessing this class triggers static registration.
    }

    public static Collection<ProfessionStat> all() {
        return Collections.unmodifiableCollection(PROFESSIONS.values());
    }

    public static Optional<ProfessionStat> byId(Identifier id) {
        return Optional.ofNullable(PROFESSIONS.get(id));
    }

    private static <T extends ProfessionStat> T register(T profession) {
        ProfessionStat previous = PROFESSIONS.putIfAbsent(profession.id(), profession);

        if (previous != null) {
            throw new IllegalStateException("Duplicate profession id: " + profession.id());
        }

        return profession;
    }
}
