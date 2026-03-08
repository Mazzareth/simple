package app.masterwork.simple.stats.strength;

import net.minecraft.server.level.ServerPlayer;

import app.masterwork.simple.stats.PlayerStats;
import app.masterwork.simple.stats.progression.ProfessionProgress;

public record StrengthData(ProfessionProgress progress, double attackDamageBonus, double attackKnockbackBonus, double miningEfficiencyBonus) {
    public static StrengthData fromPlayer(ServerPlayer player) {
        return fromProgress(PlayerStats.strengthProgress(player));
    }

    public static StrengthData fromProgress(ProfessionProgress progress) {
        return new StrengthData(
                progress,
                StrengthBonuses.attackDamageBonus(progress.level()),
                StrengthBonuses.attackKnockbackBonus(progress.level()),
                StrengthBonuses.miningEfficiencyBonus(progress.level())
        );
    }
}
