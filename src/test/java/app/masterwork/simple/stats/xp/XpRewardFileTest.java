package app.masterwork.simple.stats.xp;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class XpRewardFileTest {
    @Test
    void codecParsesTypedRewardEntries() {
        String json = """
                {
                  "entries": [
                    {
                      "match": {
                        "type": "block_tag",
                        "id": "minecraft:lapis_ores"
                      },
                      "xp": 40
                    }
                  ]
                }
                """;

        XpRewardFile file = XpRewardFile.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json))
                .getOrThrow();

        assertEquals(1, file.entries().size());
        assertEquals(XpRewardTargetType.BLOCK_TAG, file.entries().getFirst().match().type());
        assertEquals(40, file.entries().getFirst().xp());
    }

    @Test
    void codecRejectsNonPositiveXp() {
        String json = """
                {
                  "entries": [
                    {
                      "match": {
                        "type": "item",
                        "id": "minecraft:charcoal"
                      },
                      "xp": 0
                    }
                  ]
                }
                """;

        assertTrue(XpRewardFile.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(json)).error().isPresent());
    }
}
