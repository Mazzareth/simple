package app.masterwork.simple.stats.xp;

import java.util.Objects;
import java.util.Set;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

record XpRewardRule(Identifier professionId, String source, XpRewardTarget target, int xp, int priority, Set<Identifier> coveredIds) {
    XpRewardRule {
        Objects.requireNonNull(professionId, "professionId");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(target, "target");
        coveredIds = Set.copyOf(coveredIds);
    }

    boolean matches(XpRewardContext context) {
        if (coveredIds.contains(context.targetId())) {
            return true;
        }

        return switch (target.type()) {
            case BLOCK, ITEM -> target.id().equals(context.targetId());
            case BLOCK_TAG -> context instanceof BlockBreakXpContext blockContext
                    && blockContext.state().is(TagKey.create(Registries.BLOCK, target.id()));
            case ITEM_TAG -> context instanceof SmeltingXpContext smeltingContext
                    && smeltingContext.extractedStack().is(TagKey.create(Registries.ITEM, target.id()));
        };
    }

    boolean exactMatch() {
        return target.exactMatch();
    }
}
