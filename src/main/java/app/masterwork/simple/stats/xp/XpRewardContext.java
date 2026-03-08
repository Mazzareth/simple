package app.masterwork.simple.stats.xp;

import net.minecraft.resources.Identifier;

public interface XpRewardContext {
    XpRewardActivity activity();

    Identifier targetId();

    default int multiplier() {
        return 1;
    }
}
