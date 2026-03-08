package app.masterwork.simple.stats.strength;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class StrengthActionTracker {
    private final long cooldownTicks;
    private final int maxEntriesPerPlayer;
    private final Map<UUID, Deque<RecentBlockAction>> recentActions = new HashMap<>();

    public StrengthActionTracker(long cooldownTicks, int maxEntriesPerPlayer) {
        this.cooldownTicks = cooldownTicks;
        this.maxEntriesPerPlayer = maxEntriesPerPlayer;
    }

    public boolean allowAndRecord(UUID playerId, ResourceKey<Level> dimension, BlockPos pos, Identifier blockId, Identifier itemId, long tick) {
        Deque<RecentBlockAction> actions = recentActions.computeIfAbsent(playerId, ignored -> new ArrayDeque<>());
        purge(actions, tick);

        for (RecentBlockAction action : actions) {
            if (action.conflictsWith(dimension, pos, blockId, itemId)) {
                return false;
            }
        }

        actions.addLast(new RecentBlockAction(dimension, pos.immutable(), blockId, itemId, tick));

        while (actions.size() > maxEntriesPerPlayer) {
            actions.removeFirst();
        }

        return true;
    }

    public void clear(UUID playerId) {
        recentActions.remove(playerId);
    }

    private void purge(Deque<RecentBlockAction> actions, long tick) {
        Iterator<RecentBlockAction> iterator = actions.iterator();

        while (iterator.hasNext()) {
            if ((tick - iterator.next().tick()) > cooldownTicks) {
                iterator.remove();
            }
        }
    }

    private record RecentBlockAction(ResourceKey<Level> dimension, BlockPos pos, Identifier blockId, Identifier itemId, long tick) {
        private boolean conflictsWith(ResourceKey<Level> otherDimension, BlockPos otherPos, Identifier otherBlockId, Identifier otherItemId) {
            return dimension.equals(otherDimension)
                    && pos.equals(otherPos)
                    && (blockId.equals(otherBlockId) || itemId.equals(otherItemId));
        }
    }
}
