package com.healthfps;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthFpsMod implements ModInitializer {
    public static final String MOD_ID = "healthfps";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("HealthFPS loaded.");
    }
}
