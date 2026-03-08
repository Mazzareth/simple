package app.masterwork.simple.stats.xp;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XpRewardCompilerTest {
    private static final Identifier STONE = Identifier.parse("minecraft:stone");
    private static final Identifier DIRT = Identifier.parse("minecraft:dirt");
    private static final Identifier IRON_INGOT = Identifier.parse("minecraft:iron_ingot");

    @Test
    void codecParsesExplicitMatchSchema() {
        String json = """
                {
                  "entries": [
                    {
                      "match": {
                        "type": "block_tag",
                        "id": "minecraft:lapis_ores"
                      },
                      "xp": 40,
                      "priority": 2
                    }
                  ]
                }
                """;

        XpRewardFile file = XpRewardFile.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json))
                .getOrThrow();

        assertEquals(1, file.entries().size());
        assertEquals(XpRewardTargetType.BLOCK_TAG, file.entries().getFirst().match().type());
        assertEquals(Identifier.parse("minecraft:lapis_ores"), file.entries().getFirst().match().id());
        assertEquals(40, file.entries().getFirst().xp());
        assertEquals(2, file.entries().getFirst().priority());
    }

    @Test
    void pathParserExtractsActivityAndProfessionId() {
        XpRewardSource source = XpRewardSource.fromFileId(XpRewardActivity.BLOCK_BREAK, Identifier.parse("simple:simple/quarrying/ores"));

        assertEquals(XpRewardActivity.BLOCK_BREAK, source.activity());
        assertEquals(Identifier.parse("simple:quarrying"), source.professionId());
    }

    @Test
    void compilerPrefersHigherPriorityThenExactMatches() {
        XpRewardTables exactPreferred = XpRewardCompiler.compileResolved(List.of(
                rule("simple:strength", "tag", XpRewardTargetType.BLOCK_TAG, "test:stone_family", 2, 0, STONE),
                rule("simple:strength", "exact", XpRewardTargetType.BLOCK, "minecraft:stone", 5, 0, STONE)
        ));

        assertEquals(Map.of(Identifier.parse("simple:strength"), 5), exactPreferred.resolveAll(new TestContext(XpRewardActivity.BLOCK_BREAK, STONE, 1)));

        XpRewardTables higherPriorityPreferred = XpRewardCompiler.compileResolved(List.of(
                rule("simple:strength", "tag", XpRewardTargetType.BLOCK_TAG, "test:stone_family", 2, 1, STONE),
                rule("simple:strength", "exact", XpRewardTargetType.BLOCK, "minecraft:stone", 5, 0, STONE)
        ));

        assertEquals(Map.of(Identifier.parse("simple:strength"), 2), higherPriorityPreferred.resolveAll(new TestContext(XpRewardActivity.BLOCK_BREAK, STONE, 1)));
    }

    @Test
    void compilerRejectsAmbiguousTies() {
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> XpRewardCompiler.compileResolved(List.of(
                rule("simple:mining", "a", XpRewardTargetType.BLOCK_TAG, "test:ore_family_a", 10, 0, STONE),
                rule("simple:mining", "b", XpRewardTargetType.BLOCK_TAG, "test:ore_family_b", 12, 0, STONE)
        )));

        assertTrue(error.getMessage().contains("Ambiguous xp reward rules"));
    }

    @Test
    void resolveReturnsEmptyWhenNothingMatches() {
        XpRewardTables tables = XpRewardCompiler.compileResolved(List.of(
                rule("simple:quarrying", "stone", XpRewardTargetType.BLOCK, "minecraft:stone", 4, 0, STONE)
        ));

        assertTrue(tables.resolveAll(new TestContext(XpRewardActivity.BLOCK_BREAK, DIRT, 1)).isEmpty());
    }

    @Test
    void oneActionCanAwardMultipleProfessions() {
        XpRewardTables blockTables = XpRewardCompiler.compileResolved(List.of(
                rule("simple:strength", "strength", XpRewardTargetType.BLOCK, "minecraft:stone", 2, 0, STONE),
                rule("simple:quarrying", "quarrying", XpRewardTargetType.BLOCK, "minecraft:stone", 4, 0, STONE)
        ));
        XpRewardTables smeltingTables = XpRewardCompiler.compileResolved(List.of(
                rule("simple:mining", "smelt", XpRewardTargetType.ITEM, "minecraft:iron_ingot", 8, 0, IRON_INGOT)
        ));

        assertEquals(
                Map.of(
                        Identifier.parse("simple:strength"), 2,
                        Identifier.parse("simple:quarrying"), 4
                ),
                blockTables.resolveAll(new TestContext(XpRewardActivity.BLOCK_BREAK, STONE, 1))
        );
        assertEquals(
                Map.of(Identifier.parse("simple:mining"), 24),
                smeltingTables.resolveAll(new TestContext(XpRewardActivity.SMELTING, IRON_INGOT, 3))
        );
    }

    private static XpRewardRule rule(
            String professionId,
            String source,
            XpRewardTargetType type,
            String targetId,
            int xp,
            int priority,
            Identifier... coveredIds
    ) {
        return new XpRewardRule(
                Identifier.parse(professionId),
                source,
                new XpRewardTarget(type, Identifier.parse(targetId)),
                xp,
                priority,
                Set.of(coveredIds)
        );
    }

    private record TestContext(XpRewardActivity activity, Identifier targetId, int multiplier) implements XpRewardContext {
    }
}
