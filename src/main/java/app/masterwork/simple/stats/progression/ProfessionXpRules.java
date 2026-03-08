package app.masterwork.simple.stats.progression;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;

public final class ProfessionXpRules {
    private ProfessionXpRules() {
    }

    public static boolean canGainGameplayXp(ServerPlayer player) {
        GameType gameMode = player.gameMode.getGameModeForPlayer();
        return gameMode == GameType.SURVIVAL || gameMode == GameType.ADVENTURE;
    }
}
