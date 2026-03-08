package app.masterwork.simple.stats.xp;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XpRewardTablesTest {
    private static final Identifier STRENGTH = Identifier.fromNamespaceAndPath("simple", "strength");
    private static final Identifier MINING = Identifier.fromNamespaceAndPath("simple", "mining");
    private static final Identifier STONE = Identifier.fromNamespaceAndPath("minecraft", "stone");
    private static final Identifier COBBLESTONE = Identifier.fromNamespaceAndPath("minecraft", "cobblestone");

    @Test
    void exactMatchesBeatTagMatchesAtSamePriority() {
        XpRewardRule exactRule = new XpRewardRule(
                STRENGTH,
                "exact",
                new XpRewardTarget(XpRewardTargetType.BLOCK, STONE),
                7,
                0,
                Set.of(STONE)
        );
        XpRewardRule tagRule = new XpRewardRule(
                STRENGTH,
                "tag",
                new XpRewardTarget(XpRewardTargetType.BLOCK_TAG, Identifier.fromNamespaceAndPath("c", "stones")),
                3,
                0,
                Set.of(STONE, COBBLESTONE)
        );
        XpRewardRule miningRule = new XpRewardRule(
                MINING,
                "other-profession",
                new XpRewardTarget(XpRewardTargetType.BLOCK_TAG, Identifier.fromNamespaceAndPath("c", "stones")),
                5,
                0,
                Set.of(STONE, COBBLESTONE)
        );

        XpRewardTables tables = XpRewardCompiler.compileResolved(List.of(exactRule, tagRule, miningRule));
        Map<Identifier, Integer> rewards = tables.resolveAll(new TestContext(STONE, 1));

        assertEquals(7, rewards.get(STRENGTH));
        assertEquals(5, rewards.get(MINING));
    }

    @Test
    void returnsNoRewardsWhenNothingMatches() {
        XpRewardTables tables = XpRewardCompiler.compileResolved(List.of(
                new XpRewardRule(
                        STRENGTH,
                        "stone",
                        new XpRewardTarget(XpRewardTargetType.BLOCK, STONE),
                        4,
                        0,
                        Set.of(STONE)
                )
        ));

        assertTrue(tables.resolveAll(new TestContext(COBBLESTONE, 1)).isEmpty());
    }

    @Test
    void rejectsAmbiguousSameRankRulesForOneProfession() {
        XpRewardRule left = new XpRewardRule(
                STRENGTH,
                "left",
                new XpRewardTarget(XpRewardTargetType.BLOCK_TAG, Identifier.fromNamespaceAndPath("c", "stones_a")),
                4,
                0,
                Set.of(STONE)
        );
        XpRewardRule right = new XpRewardRule(
                STRENGTH,
                "right",
                new XpRewardTarget(XpRewardTargetType.BLOCK_TAG, Identifier.fromNamespaceAndPath("c", "stones_b")),
                6,
                0,
                Set.of(STONE)
        );

        assertThrows(IllegalStateException.class, () -> XpRewardCompiler.compileResolved(List.of(left, right)));
    }

    private record TestContext(Identifier targetId, int multiplier) implements XpRewardContext {
        @Override
        public XpRewardActivity activity() {
            return XpRewardActivity.BLOCK_BREAK;
        }
    }
}
