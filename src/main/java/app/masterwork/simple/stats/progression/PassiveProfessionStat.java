package app.masterwork.simple.stats.progression;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

public final class PassiveProfessionStat extends ProfessionStat {
    public PassiveProfessionStat(String path) {
        super(path);
    }

    public PassiveProfessionStat(Identifier id) {
        super(id);
    }

    @Override
    protected void onProgressChanged(ServerPlayer player, ProfessionProgress oldValue, ProfessionProgress newValue) {
    }
}
