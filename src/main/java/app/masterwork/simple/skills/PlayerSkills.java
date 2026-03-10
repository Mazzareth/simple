package app.masterwork.simple.skills;

import java.util.List;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import app.masterwork.simple.skills.sync.SkillSnapshot;
import app.masterwork.simple.stats.progression.ProfessionXpRules;

public final class PlayerSkills {
    private PlayerSkills() {
    }

    public static SkillProfile profile(ServerPlayer player) {
        return SkillProfileRegistry.PROFILE.get(player);
    }

    public static SkillProfile awardGameplayBxp(ServerPlayer player, int amount) {
        if (amount <= 0 || !ProfessionXpRules.canGainGameplayXp(player)) {
            return profile(player);
        }

        return awardBxp(player, amount);
    }

    public static SkillProfile awardBxp(ServerPlayer player, int amount) {
        if (amount <= 0) {
            return profile(player);
        }

        return SkillProfileRegistry.PROFILE.modify(player, profile -> SkillProgression.awardBxp(profile, amount));
    }

    public static SkillProfile unlock(ServerPlayer player, Identifier skillId) {
        return SkillProfileRegistry.PROFILE.modify(player, profile -> SkillProgression.unlock(profile, skillId));
    }

    public static void reset(ServerPlayer player) {
        SkillProfileRegistry.PROFILE.reset(player);
    }

    public static boolean hasUnlocked(ServerPlayer player, Identifier skillId) {
        return profile(player).hasUnlocked(skillId);
    }

    public static int ep(ServerPlayer player) {
        return SkillProgression.ep(profile(player));
    }

    public static List<SkillSnapshot> snapshots(ServerPlayer player) {
        SkillProfile profile = profile(player);
        return SkillRegistry.all().stream()
                .map(skill -> new SkillSnapshot(skill.id(), profile.hasUnlocked(skill.id())))
                .toList();
    }
}
