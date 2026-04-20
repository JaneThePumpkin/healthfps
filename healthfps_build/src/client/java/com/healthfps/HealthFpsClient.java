package com.healthfps;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class HealthFpsClient implements ClientModInitializer {

    private static final int UPDATE_INTERVAL_TICKS = 4;
    public static final int MIN_FPS = 10;
    public static float healthFraction = 1.0f;

    private int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (tickCounter < UPDATE_INTERVAL_TICKS) return;
            tickCounter = 0;

            Player player = client.player;
            if (player == null) {
                healthFraction = 1.0f;
                return;
            }

            float currentHealth = player.getHealth();
            float maxHealth = player.getMaxHealth();
            healthFraction = Math.max(0.0f, Math.min(currentHealth / maxHealth, 1.0f));

            int targetFps = calculateFpsCap(healthFraction);

            try {
                Minecraft.getInstance().options.framerateLimit().set(targetFps);
            } catch (Exception e) {
                HealthFpsMod.LOGGER.error("Failed to set FPS: " + e.getMessage());
            }
        });
    }

    public static int calculateFpsCap(float fraction) {
        if (fraction <= 0.1f) return MIN_FPS;
        float t = (fraction - 0.1f) / 0.9f;
        t = (float) Math.pow(t, 2.0);
        return Math.max(MIN_FPS, (int)(MIN_FPS + t * (260 - MIN_FPS)));
    }
}
