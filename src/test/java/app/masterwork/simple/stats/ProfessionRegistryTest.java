package app.masterwork.simple.stats;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProfessionRegistryTest {
    @Test
    void newProfessionIdsAreRegistered() {
        assertTrue(ProfessionRegistry.byId(Identifier.fromNamespaceAndPath("simple", "quarrying")).isPresent());
        assertTrue(ProfessionRegistry.byId(Identifier.fromNamespaceAndPath("simple", "mining")).isPresent());
        assertTrue(ProfessionRegistry.byId(Identifier.fromNamespaceAndPath("simple", "woodcutting")).isPresent());
    }

    @Test
    void statRegistryFacadeRemainsLimitedToLegacyStats() {
        assertTrue(StatRegistry.byId(Identifier.fromNamespaceAndPath("simple", "quarrying")).isEmpty());
        assertTrue(StatRegistry.byId(Identifier.fromNamespaceAndPath("simple", "mining")).isEmpty());
        assertTrue(StatRegistry.byId(Identifier.fromNamespaceAndPath("simple", "woodcutting")).isEmpty());
    }
}
