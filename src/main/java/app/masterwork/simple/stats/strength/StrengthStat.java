package app.masterwork.simple.stats.strength;

import net.minecraft.server.level.ServerPlayer;

import app.masterwork.simple.stats.progression.ProfessionProgress;
import app.masterwork.simple.stats.progression.ProfessionStat;

public final class StrengthStat extends ProfessionStat {
    public StrengthStat() {
        super("strength");
    }

    @Override
    protected void onProgressChanged(ServerPlayer player, ProfessionProgress oldValue, ProfessionProgress newValue) {
        StrengthEffects.apply(player, newValue);
    }
}
