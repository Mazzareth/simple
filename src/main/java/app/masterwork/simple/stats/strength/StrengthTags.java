package app.masterwork.simple.stats.strength;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

import app.masterwork.simple.Simple;

public final class StrengthTags {
    public static final TagKey<Block> TRAINING_BLOCKS = TagKey.create(Registries.BLOCK, Identifier.parse(Simple.MOD_ID + ":strength_training_blocks"));
    public static final TagKey<Block> BUILDING_BLOCKS = TagKey.create(Registries.BLOCK, Identifier.parse(Simple.MOD_ID + ":strength_building_blocks"));

    private StrengthTags() {
    }
}
