package app.masterwork.simple.stats.xp;

import java.util.Objects;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.BlockState;

public record BlockBreakXpContext(
        BlockState state,
        boolean correctToolForDrops,
        Identifier targetId
) implements XpRewardContext {
    public BlockBreakXpContext {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(targetId, "targetId");
    }

    public static BlockBreakXpContext create(net.minecraft.server.level.ServerPlayer player, BlockState state) {
        return new BlockBreakXpContext(
                state,
                player.hasCorrectToolForDrops(state),
                BuiltInRegistries.BLOCK.getKey(state.getBlock())
        );
    }

    @Override
    public XpRewardActivity activity() {
        return XpRewardActivity.BLOCK_BREAK;
    }
}
