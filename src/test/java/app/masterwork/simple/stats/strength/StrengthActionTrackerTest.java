package app.masterwork.simple.stats.strength;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrengthActionTrackerTest {
    @Test
    void blocksRepeatedActionsWithinCooldownAndAllowsNewOnesAfterwards() {
        StrengthActionTracker tracker = new StrengthActionTracker(40L, 8);
        UUID playerId = UUID.randomUUID();
        ResourceKey<Level> overworld = ResourceKey.create(Registries.DIMENSION, Identifier.parse("minecraft:overworld"));
        BlockPos pos = new BlockPos(1, 2, 3);
        Identifier blockId = Identifier.parse("minecraft:stone");
        Identifier itemId = Identifier.parse("minecraft:wooden_pickaxe");

        assertTrue(tracker.allowAndRecord(playerId, overworld, pos, blockId, itemId, 100L));
        assertFalse(tracker.allowAndRecord(playerId, overworld, pos, blockId, itemId, 120L));
        assertTrue(tracker.allowAndRecord(playerId, overworld, pos.offset(1, 0, 0), blockId, itemId, 120L));
        assertTrue(tracker.allowAndRecord(playerId, overworld, pos, blockId, itemId, 141L));
    }
}
