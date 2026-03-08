package app.masterwork.simple.stats.progression;

import net.minecraft.server.level.ServerPlayer;

import app.masterwork.simple.stats.AbstractStat;

public abstract class ProfessionStat extends AbstractStat<ProfessionProgress> {
    protected ProfessionStat(String path) {
        super(path, ProfessionProgress.CODEC, ProfessionProgress.ZERO);
    }

    @Override
    protected final ProfessionProgress sanitize(ProfessionProgress value) {
        return ProfessionProgression.sanitize(value);
    }

    @Override
    protected final void onSet(ServerPlayer player, ProfessionProgress oldValue, ProfessionProgress newValue) {
        onProgressChanged(player, oldValue, newValue);
    }

    protected abstract void onProgressChanged(ServerPlayer player, ProfessionProgress oldValue, ProfessionProgress newValue);
}
