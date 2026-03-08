package app.masterwork.simple.stats.agility;

import net.minecraft.server.level.ServerPlayer;

import app.masterwork.simple.stats.progression.ProfessionProgress;
import app.masterwork.simple.stats.progression.ProfessionStat;

public final class AgilityStat extends ProfessionStat {
    public AgilityStat() {
        super("agility");
    }

    @Override
    protected void onProgressChanged(ServerPlayer player, ProfessionProgress oldValue, ProfessionProgress newValue) {
        AgilityEffects.apply(player, newValue);
    }
}
