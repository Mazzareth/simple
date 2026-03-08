package app.masterwork.simple.stats;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerStatsFacadeTest {
    @Test
    void thirstIsNoLongerRegistered() {
        assertTrue(StatRegistry.byId(Identifier.parse("simple:thirst")).isEmpty());
    }

    @Test
    void thirstAccessorsAreGoneFromPlayerStats() {
        boolean hasThirstMethod = Arrays.stream(PlayerStats.class.getDeclaredMethods())
                .map(Method::getName)
                .anyMatch(name -> name.equalsIgnoreCase("thirst"));

        assertFalse(hasThirstMethod);
    }
}
