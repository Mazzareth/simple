package app.masterwork.simple;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

import app.masterwork.simple.skills.SkillCommands;
import app.masterwork.simple.skills.SkillProfileRegistry;
import app.masterwork.simple.skills.SkillRegistry;
import app.masterwork.simple.skills.sync.PlayerSkillsNetworking;
import app.masterwork.simple.stats.ProfessionRegistry;
import app.masterwork.simple.stats.PlayerStatEffects;
import app.masterwork.simple.stats.agility.AgilityEvents;
import app.masterwork.simple.stats.strength.StrengthEvents;
import app.masterwork.simple.stats.sync.PlayerStatsNetworking;
import app.masterwork.simple.stats.xp.XpRewards;

public class Simple implements ModInitializer {
    public static final String MOD_ID = "simple";

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ProfessionRegistry.bootstrap();
        SkillRegistry.bootstrap();
        SkillProfileRegistry.bootstrap();
        PlayerStatsNetworking.register();
        PlayerSkillsNetworking.register();
        XpRewards.register();
        PlayerStatEffects.register();
        AgilityEvents.register();
        StrengthEvents.register();
        SkillCommands.register();
    }
}
