package app.masterwork.simple.stats.xp;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.resources.Identifier;

final class XpRewardTables {
    private static final XpRewardTables EMPTY = new XpRewardTables(Map.of());

    private final Map<Identifier, List<XpRewardRule>> rulesByProfession;

    XpRewardTables(Map<Identifier, List<XpRewardRule>> rulesByProfession) {
        Map<Identifier, List<XpRewardRule>> copy = new LinkedHashMap<>();
        rulesByProfession.forEach((professionId, rules) -> copy.put(professionId, List.copyOf(rules)));
        this.rulesByProfession = Map.copyOf(copy);
    }

    static XpRewardTables empty() {
        return EMPTY;
    }

    boolean hasMatches(XpRewardContext context) {
        return !resolveAll(context).isEmpty();
    }

    Map<Identifier, Integer> resolveAll(XpRewardContext context) {
        if (context.multiplier() <= 0) {
            return Map.of();
        }

        Map<Identifier, Integer> rewards = new LinkedHashMap<>();

        for (Map.Entry<Identifier, List<XpRewardRule>> entry : rulesByProfession.entrySet()) {
            XpRewardRule bestRule = null;
            List<XpRewardRule> sameRankMatches = new ArrayList<>();

            for (XpRewardRule rule : entry.getValue()) {
                if (!rule.matches(context)) {
                    continue;
                }

                if (bestRule == null) {
                    bestRule = rule;
                    sameRankMatches.clear();
                    sameRankMatches.add(rule);
                    continue;
                }

                int comparison = compare(rule, bestRule);

                if (comparison > 0) {
                    bestRule = rule;
                    sameRankMatches.clear();
                    sameRankMatches.add(rule);
                } else if (comparison == 0) {
                    sameRankMatches.add(rule);
                }
            }

            if (sameRankMatches.size() > 1) {
                throw new IllegalStateException("Ambiguous xp reward resolution for profession " + entry.getKey()
                        + ": " + sameRankMatches.get(0).source() + " conflicts with " + sameRankMatches.get(1).source());
            }

            if (bestRule != null) {
                rewards.put(entry.getKey(), Math.multiplyExact(bestRule.xp(), context.multiplier()));
            }
        }

        return Map.copyOf(rewards);
    }

    private static int compare(XpRewardRule left, XpRewardRule right) {
        int priorityComparison = Integer.compare(left.priority(), right.priority());

        if (priorityComparison != 0) {
            return priorityComparison;
        }

        return Boolean.compare(left.exactMatch(), right.exactMatch());
    }
}
