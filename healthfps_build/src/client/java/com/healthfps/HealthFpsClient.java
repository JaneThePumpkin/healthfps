package com.healthfps;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.entity.player.PlayerEntity;

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

            PlayerEntity player = client.player;
            if (player == null) return;

            float currentHealth = player.getHealth();
            float maxHealth = player.getMaxHealth();

            int targetFps = calculateFpsCap(currentHealth, maxHealth);

            if (lastFps != targetFps) {
                lastFps = targetFps;
                SimpleOption<Integer> maxFpsOption = client.options.getMaxFps();
                maxFpsOption.setValue(targetFps);
            }
        });
    }

    private int calculateFpsCap(float health, float maxHealth) {
        health = Math.max(0.0f, Math.min(health, maxHealth));

        float fraction = health / maxHealth;
        float lowFraction = LOW_HEALTH_THRESHOLD / maxHealth;

        if (fraction <= lowFraction) {
            return MIN_FPS;
        }

        int maxFps = 260;
        float t = (fraction - lowFraction) / (1.0f - lowFraction);
        t = (float) Math.pow(t, 0.6);

        return Math.max(MIN_FPS, (int)(MIN_FPS + t * (maxFps - MIN_FPS)));
    }
}
