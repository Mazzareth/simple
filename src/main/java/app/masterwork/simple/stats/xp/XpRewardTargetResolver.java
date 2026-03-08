package app.masterwork.simple.stats.xp;

import java.util.Set;

import net.minecraft.resources.Identifier;

@FunctionalInterface
interface XpRewardTargetResolver {
    Set<Identifier> resolveTargets(XpRewardActivity activity, XpRewardTarget match, String sourceDescription);
}
