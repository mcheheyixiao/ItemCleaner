package org.stellarvan.itemcleaner;


import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemCleaner implements ModInitializer {
    public static final String MOD_ID = "itemcleaner";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID); // 添加LOGGER
    public static CleanupConfig config;
    public static CleanupTimer cleanupTimer;

    @Override
    public void onInitialize() {
        config = CleanupConfig.loadConfig();
        cleanupTimer = new CleanupTimer();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CleanupCommands.register(dispatcher, registryAccess, environment);
        });

        LOGGER.info("ItemCleaner 模组已加载");
    }

    public static void setServer(net.minecraft.server.MinecraftServer server) {
        if (cleanupTimer != null) {
            cleanupTimer.setServer(server);
        }
    }

    public static void saveConfig() {
        if (config != null) {
            CleanupConfig.saveConfig(config);
        }
    }
}
