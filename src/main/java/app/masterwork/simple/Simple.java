package app.masterwork.simple;

import net.fabricmc.api.ModInitializer;

import app.masterwork.simple.stats.PlayerStatEffects;
import app.masterwork.simple.stats.StatRegistry;
import app.masterwork.simple.stats.agility.AgilityEvents;
import app.masterwork.simple.stats.strength.StrengthEvents;

public class Simple implements ModInitializer {
    public static final String MOD_ID = "simple";

    @Override
    public void onInitialize() {
        StatRegistry.bootstrap();
        PlayerStatEffects.register();
        AgilityEvents.register();
        StrengthEvents.register();
    }
}
