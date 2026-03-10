package app.masterwork.simple.skills;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SkillRegistryTest {
    @Test
    void starterRegistryHasExpectedOrderAndCount() {
        List<SkillDefinition> definitions = List.copyOf(SkillRegistry.all());

        assertEquals(8, definitions.size());
        assertEquals(SkillRegistry.HEAT_RESISTANCE.id(), definitions.get(0).id());
        assertEquals(SkillRegistry.GREAT_SAGE.id(), definitions.get(definitions.size() - 1).id());
    }

    @Test
    void registerRejectsDuplicateIds() throws ReflectiveOperationException {
        Method register = SkillRegistry.class.getDeclaredMethod("register", SkillDefinition.class);
        register.setAccessible(true);

        InvocationTargetException exception = assertThrows(
                InvocationTargetException.class,
                () -> register.invoke(null, new SkillDefinition(
                        SkillRegistry.HEAT_RESISTANCE.id(),
                        SkillTier.COMMON,
                        SkillTier.COMMON.epValue(),
                        0,
                        List.of()
                ))
        );

        assertInstanceOf(IllegalStateException.class, exception.getCause());
    }
}
