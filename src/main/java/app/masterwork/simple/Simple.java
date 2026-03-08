package app.masterwork.simple;

import net.fabricmc.api.ModInitializer;

import app.masterwork.simple.stats.StatRegistry;
import app.masterwork.simple.stats.agility.AgilityEvents;

public class Simple implements ModInitializer {
    public static final String MOD_ID = "simple";

    @Override
    public void onInitialize() {
        StatRegistry.bootstrap();
        AgilityEvents.register();
    }
}
