package app.masterwork.simple.stats.xp;

import org.junit.jupiter.api.Test;

import net.minecraft.resources.Identifier;

import static org.junit.jupiter.api.Assertions.assertEquals;

class XpRewardSourceTest {
    @Test
    void parsesProfessionIdFromRewardFilePath() {
        XpRewardSource source = XpRewardSource.fromFileId(
                XpRewardActivity.BLOCK_BREAK,
                Identifier.fromNamespaceAndPath("simple", "simple/quarrying/ores")
        );

        assertEquals(Identifier.fromNamespaceAndPath("simple", "quarrying"), source.professionId());
    }

    @Test
    void preservesNestedProfessionPaths() {
        XpRewardSource source = XpRewardSource.fromFileId(
                XpRewardActivity.SMELTING,
                Identifier.fromNamespaceAndPath("pack", "simple/tools/mining/example")
        );

        assertEquals(Identifier.fromNamespaceAndPath("simple", "tools/mining"), source.professionId());
    }
}
