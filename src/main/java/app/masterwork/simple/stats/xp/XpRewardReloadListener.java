package app.masterwork.simple.stats.xp;

import java.util.Map;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

final class XpRewardReloadListener extends SimpleJsonResourceReloadListener<XpRewardFile> {
    private final XpRewardActivity activity;
    private final HolderLookup.Provider registries;

    XpRewardReloadListener(XpRewardActivity activity, HolderLookup.Provider registries) {
        super(XpRewardFile.CODEC, FileToIdConverter.json(activity.resourcePath()));
        this.activity = activity;
        this.registries = registries;
    }

    @Override
    protected void apply(Map<Identifier, XpRewardFile> prepared, ResourceManager resourceManager, ProfilerFiller profiler) {
        XpRewards.replaceTables(activity, XpRewardCompiler.compile(activity, prepared, resourceManager, registries));
    }
}
