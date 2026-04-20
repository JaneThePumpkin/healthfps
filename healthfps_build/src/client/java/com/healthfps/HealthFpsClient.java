package com.healthfps;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class HealthFpsClient implements ClientModInitializer {

    private static final int UPDATE_INTERVAL_TICKS = 4;
    private static final int MIN_FPS = 5;
    private static final float LOW_HEALTH_THRESHOLD = 2.0f;

    private int tickCounter = 0;
    private int lastFps = -1;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            tickCounter++;
            if (tickCounter < UPDATE_INTERVAL_TICKS) return;
            tickCounter = 0;

            Player player = client.player;
            if (player == null) return;

            float currentHealth = player.getHealth();
            float maxHealth = player.getMaxHealth();
            int targetFps = calculateFpsCap(currentHealth, maxHealth);

            if (lastFps != targetFps) {
                lastFps = targetFps;
                try {
                    var options = Minecraft.getInstance().options;
                    try {
                        // Try Mojang mappings name first
                        options.getClass().getMethod("framerateLimit").invoke(options);
                        var opt = options.framerateLimit();
                        opt.set(targetFps);
                    } catch (NoSuchMethodException e) {
                        // Try reflection to find any field containing fps/framerate
                        for (var field : options.getClass().getDeclaredFields()) {
                            String name = field.getName().toLowerCase();
                            if (name.contains("fps") || name.contains("framerate") || name.contains("maxfps")) {
                                field.setAccessible(true);
                                Object val = field.get(options);
                                if (val != null) {
                                    try {
                                        val.getClass().getMethod("set", Object.class).invoke(val, targetFps);
                                        HealthFpsMod.LOGGER.info("Set FPS via field: " + field.getName());
                                        break;
                                    } catch (Exception ex) {
                                        val.getClass().getMethod("set", int.class).invoke(val, targetFps);
                                        HealthFpsMod.LOGGER.info("Set FPS via field (int): " + field.getName());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    HealthFpsMod.LOGGER.info("Health: " + currentHealth + "/" + maxHealth + " -> FPS cap: " + targetFps);
                } catch (Exception e) {
                    HealthFpsMod.LOGGER.error("Failed to set FPS cap: " + e.getMessage());
                }
            }
        });
    }

    private int calculateFpsCap(float health, float maxHealth) {
        health = Math.max(0.0f, Math.min(health, maxHealth));
        float fraction = health / maxHealth;
        float lowFraction = LOW_HEALTH_THRESHOLD / maxHealth;
        if (fraction <= lowFraction) return MIN_FPS;
        int maxFps = 260;
        float t = (fraction - lowFraction) / (1.0f - lowFraction);
        t = (float) Math.pow(t, 0.6);
        return Math.max(MIN_FPS, (int)(MIN_FPS + t * (maxFps - MIN_FPS)));
    }
}
