package app.masterwork.simple;

import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;

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
        PlayerStatsNetworking.register();
        XpRewards.register();
        PlayerStatEffects.register();
        AgilityEvents.register();
        StrengthEvents.register();
    }
}
