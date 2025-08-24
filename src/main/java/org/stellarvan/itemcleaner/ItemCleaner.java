package org.stellarvan.itemcleaner;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemCleaner implements ModInitializer {
    public static final String MOD_ID = "itemcleaner";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static CleanupConfig config;
    public static CleanupTimer cleanupTimer;
    private static net.minecraft.server.MinecraftServer server;

    @Override
    public void onInitialize() {
        // 加载配置
        config = CleanupConfig.loadConfig();

        // 初始化语言系统
        I18n.init();

        // 初始化清理计时器
        cleanupTimer = new CleanupTimer();

        // 注册命令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CleanupCommands.register(dispatcher, registryAccess, environment);
        });

        // 注册服务器启动事件（设置服务器实例）
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ItemCleaner.setServer(server);
        });

        // 注册配置重载事件（当配置变化时重新加载语言）
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // 配置加载后重新加载语言
            I18n.reloadTranslations();
        });

        LOGGER.info("ItemCleaner 模组已加载");
    }

    public static void setServer(net.minecraft.server.MinecraftServer server) {
        ItemCleaner.server = server;
        if (cleanupTimer != null) {
            cleanupTimer.setServer(server);
        }
    }

    // 保存配置并重新加载语言
    public static void saveConfig() {
        CleanupConfig.saveConfig(config);
        // 配置保存后重新加载语言（如果语言设置有变化）
        I18n.reloadTranslations();
    }
}
