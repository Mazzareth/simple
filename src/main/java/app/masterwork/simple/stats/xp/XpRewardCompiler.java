package app.masterwork.simple.stats.xp;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import app.masterwork.simple.stats.ProfessionRegistry;

final class XpRewardCompiler {
    private XpRewardCompiler() {
    }

    static XpRewardTables compile(
            XpRewardActivity activity,
            Map<Identifier, XpRewardFile> files,
            net.minecraft.server.packs.resources.ResourceManager resourceManager,
            net.minecraft.core.HolderLookup.Provider registries
    ) {
        net.minecraft.core.HolderGetter<Block> blockGetter = registries.lookupOrThrow(Registries.BLOCK);
        net.minecraft.core.HolderGetter<Item> itemGetter = registries.lookupOrThrow(Registries.ITEM);
        Map<TagKey<Block>, List<Holder<Block>>> blockTags = TagLoader.loadTagsForRegistry(
                resourceManager,
                Registries.BLOCK,
                TagLoader.ElementLookup.fromGetters(Registries.BLOCK, blockGetter, blockGetter)
        );
        Map<TagKey<Item>, List<Holder<Item>>> itemTags = TagLoader.loadTagsForRegistry(
                resourceManager,
                Registries.ITEM,
                TagLoader.ElementLookup.fromGetters(Registries.ITEM, itemGetter, itemGetter)
        );

        return compile(activity, files, (resolvedActivity, match, sourceDescription) -> switch (match.type()) {
            case BLOCK -> resolveDirect(BuiltInRegistries.BLOCK, match.id(), sourceDescription);
            case BLOCK_TAG -> resolveLoadedTag(blockTags, Registries.BLOCK, match.id(), sourceDescription);
            case ITEM -> resolveDirect(BuiltInRegistries.ITEM, match.id(), sourceDescription);
            case ITEM_TAG -> resolveLoadedTag(itemTags, Registries.ITEM, match.id(), sourceDescription);
        });
    }

    static XpRewardTables compile(XpRewardActivity activity, Map<Identifier, XpRewardFile> files, XpRewardTargetResolver resolver) {
        List<XpRewardRule> rules = new java.util.ArrayList<>();

        for (Map.Entry<Identifier, XpRewardFile> fileEntry : files.entrySet().stream().sorted(Map.Entry.comparingByKey()).toList()) {
            XpRewardSource source = XpRewardSource.fromFileId(activity, fileEntry.getKey());

            if (ProfessionRegistry.byId(source.professionId()).isEmpty()) {
                throw new IllegalStateException("Unknown profession '" + source.professionId() + "' in xp reward file " + fileEntry.getKey());
            }

            List<XpRewardEntry> entries = fileEntry.getValue().entries();

            for (int index = 0; index < entries.size(); index++) {
                rules.add(resolveRule(activity, source, entries.get(index), index, resolver));
            }
        }

        return compileResolved(rules);
    }

    static XpRewardTables compileResolved(List<XpRewardRule> rules) {
        validateAmbiguity(rules);

        Map<Identifier, List<XpRewardRule>> byProfession = new LinkedHashMap<>();

        for (XpRewardRule rule : rules) {
            byProfession.computeIfAbsent(rule.professionId(), ignored -> new java.util.ArrayList<>()).add(rule);
        }

        return new XpRewardTables(byProfession);
    }

    private static XpRewardRule resolveRule(
            XpRewardActivity activity,
            XpRewardSource source,
            XpRewardEntry entry,
            int index,
            XpRewardTargetResolver resolver
    ) {
        if (!entry.match().type().supports(activity)) {
            throw new IllegalStateException("Target type '" + entry.match().type().serializedName() + "' is not valid for "
                    + activity.directory() + " in " + source.entrySource(index));
        }

        String entrySource = source.entrySource(index);
        Set<Identifier> coveredIds = resolver.resolveTargets(activity, entry.match(), entrySource);

        if (coveredIds.isEmpty()) {
            throw new IllegalStateException("XP reward entry resolved to no targets in " + entrySource);
        }

        return new XpRewardRule(source.professionId(), entrySource, entry.match(), entry.xp(), entry.priority(), coveredIds);
    }

    private static <T> Set<Identifier> resolveDirect(Registry<T> registry, Identifier targetId, String source) {
        if (registry.getOptional(targetId).isEmpty()) {
            throw new IllegalStateException("Unknown id '" + targetId + "' referenced by " + source);
        }

        return Set.of(targetId);
    }

    private static <T> Set<Identifier> resolveLoadedTag(
            Map<TagKey<T>, List<Holder<T>>> loadedTags,
            net.minecraft.resources.ResourceKey<? extends Registry<T>> registryKey,
            Identifier targetId,
            String source
    ) {
        TagKey<T> tagKey = TagKey.create(registryKey, targetId);
        List<Holder<T>> holders = loadedTags.get(tagKey);

        if (holders == null || holders.isEmpty()) {
            throw new IllegalStateException("Unknown tag '" + targetId + "' referenced by " + source);
        }

        Set<Identifier> coveredIds = new LinkedHashSet<>();

        for (Holder<T> holder : holders) {
            coveredIds.add(holder.unwrapKey()
                    .orElseThrow(() -> new IllegalStateException("Unbound holder in tag '" + targetId + "' referenced by " + source))
                    .identifier());
        }

        return coveredIds;
    }

    private static void validateAmbiguity(List<XpRewardRule> rules) {
        for (int leftIndex = 0; leftIndex < rules.size(); leftIndex++) {
            XpRewardRule left = rules.get(leftIndex);

            for (int rightIndex = leftIndex + 1; rightIndex < rules.size(); rightIndex++) {
                XpRewardRule right = rules.get(rightIndex);

                if (!left.professionId().equals(right.professionId())) {
                    continue;
                }

                if (left.priority() != right.priority()) {
                    continue;
                }

                if (left.exactMatch() != right.exactMatch()) {
                    continue;
                }

                Set<Identifier> overlap = new LinkedHashSet<>(left.coveredIds());
                overlap.retainAll(right.coveredIds());

                if (!overlap.isEmpty()) {
                    throw new IllegalStateException("Ambiguous xp reward rules for profession " + left.professionId()
                            + ": " + left.source() + " conflicts with " + right.source() + " on " + overlap.iterator().next());
                }
            }
        }
    }
}
