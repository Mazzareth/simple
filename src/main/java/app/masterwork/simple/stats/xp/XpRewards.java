package app.masterwork.simple.stats.xp;

import java.util.EnumMap;
import java.util.Map;

import net.fabricmc.fabric.api.resource.v1.DataResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import app.masterwork.simple.Simple;
import app.masterwork.simple.stats.PlayerStats;
import app.masterwork.simple.stats.progression.ProfessionXpRules;

public final class XpRewards {
    private static volatile Map<XpRewardActivity, XpRewardTables> tables = defaultTables();
    private static boolean registered;

    private XpRewards() {
    }

    public static void register() {
        if (registered) {
            return;
        }

        registered = true;

        for (XpRewardActivity activity : XpRewardActivity.values()) {
            Identifier listenerId = Simple.id(activity.resourcePath());
            DataResourceLoader.get().registerReloadListener(listenerId, registries -> new XpRewardReloadListener(activity, registries));
            DataResourceLoader.get().addListenerOrdering(ResourceReloaderKeys.AFTER_VANILLA, listenerId);
        }
    }

    public static boolean hasBlockBreakRewards(BlockBreakXpContext context) {
        return tables.getOrDefault(XpRewardActivity.BLOCK_BREAK, XpRewardTables.empty()).hasMatches(context);
    }

    public static void awardBlockBreak(ServerPlayer player, BlockBreakXpContext context) {
        if (!ProfessionXpRules.canGainGameplayXp(player) || !context.correctToolForDrops()) {
            return;
        }

        awardAll(player, context);
    }

    public static boolean hasSmeltingRewards(ItemStack extractedStack) {
        return !extractedStack.isEmpty()
                && tables.getOrDefault(XpRewardActivity.SMELTING, XpRewardTables.empty())
                .hasMatches(SmeltingXpContext.create(extractedStack, 1));
    }

    public static void awardSmelting(ServerPlayer player, ItemStack extractedStack, int extractedCount) {
        if (!ProfessionXpRules.canGainGameplayXp(player) || extractedStack.isEmpty() || extractedCount <= 0) {
            return;
        }

        awardAll(player, SmeltingXpContext.create(extractedStack, extractedCount));
    }

    static void replaceTables(XpRewardActivity activity, XpRewardTables updatedTables) {
        EnumMap<XpRewardActivity, XpRewardTables> nextTables = new EnumMap<>(XpRewardActivity.class);
        nextTables.putAll(tables);
        nextTables.put(activity, updatedTables);
        tables = Map.copyOf(nextTables);
    }

    static Map<Identifier, Integer> resolveAll(XpRewardContext context) {
        return tables.getOrDefault(context.activity(), XpRewardTables.empty()).resolveAll(context);
    }

    private static void awardAll(ServerPlayer player, XpRewardContext context) {
        resolveAll(context).forEach((professionId, xp) -> PlayerStats.awardXp(player, professionId, xp));
    }

    private static Map<XpRewardActivity, XpRewardTables> defaultTables() {
        EnumMap<XpRewardActivity, XpRewardTables> defaults = new EnumMap<>(XpRewardActivity.class);

        for (XpRewardActivity activity : XpRewardActivity.values()) {
            defaults.put(activity, XpRewardTables.empty());
        }

        return Map.copyOf(defaults);
    }
}
