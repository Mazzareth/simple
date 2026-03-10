package app.masterwork.simple.client;

import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;

import app.masterwork.simple.Simple;
import app.masterwork.simple.client.stats.ClientPlayerStats;
import app.masterwork.simple.client.stats.PlayerStatsScreen;

public class SimpleClient implements ClientModInitializer {
    private static final KeyMapping OPEN_STATS_SCREEN = KeyMappingHelper.registerKeyMapping(
            new KeyMapping(
                    "key.simple.open_stats",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_P,
                    KeyMapping.Category.register(Simple.id("simple"))
            )
    );

    @Override
    public void onInitializeClient() {
        ClientPlayerStats.register();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (OPEN_STATS_SCREEN.consumeClick()) {
                if (client.player == null || client.level == null) {
                    continue;
                }

                client.setScreen(new PlayerStatsScreen());
            }
        });
    }
}
