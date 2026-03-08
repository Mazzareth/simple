package app.masterwork.simple.stats.xp;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record XpRewardFile(List<XpRewardEntry> entries) {
    public static final Codec<XpRewardFile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            XpRewardEntry.CODEC.listOf().fieldOf("entries").forGetter(XpRewardFile::entries)
    ).apply(instance, XpRewardFile::new));

    public XpRewardFile {
        entries = List.copyOf(entries);
    }
}
