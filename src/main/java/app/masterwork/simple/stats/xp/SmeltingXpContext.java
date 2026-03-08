package app.masterwork.simple.stats.xp;

import java.util.Objects;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public record SmeltingXpContext(
        ItemStack extractedStack,
        int extractedCount,
        Identifier targetId
) implements XpRewardContext {
    public SmeltingXpContext {
        Objects.requireNonNull(extractedStack, "extractedStack");
        Objects.requireNonNull(targetId, "targetId");

        if (extractedCount <= 0) {
            throw new IllegalArgumentException("extractedCount must be positive");
        }
    }

    public static SmeltingXpContext create(ItemStack extractedStack, int extractedCount) {
        return new SmeltingXpContext(
                extractedStack.copy(),
                extractedCount,
                BuiltInRegistries.ITEM.getKey(extractedStack.getItem())
        );
    }

    @Override
    public XpRewardActivity activity() {
        return XpRewardActivity.SMELTING;
    }

    @Override
    public int multiplier() {
        return extractedCount;
    }
}
