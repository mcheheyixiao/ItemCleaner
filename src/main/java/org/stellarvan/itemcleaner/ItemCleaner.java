package org.stellarvan.itemcleaner;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ItemCleaner implements ModInitializer {
    public static final String MOD_ID = "itemcleaner";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static net.minecraft.server.MinecraftServer server;
    // 声明清理工具物品
    public static final Item CLEANUP_HOE = new CleanupToolItem();

    public static CleanupConfig config;
    public static CleanupTimer cleanupTimer;

    public static MinecraftServer getServer() {
        return server;
    }

    @Override
    public void onInitialize() {
        // 加载配置
        config = CleanupConfig.loadConfig();
        I18n.setLanguage(config.language);

        // 初始化清理计时器
        cleanupTimer = new CleanupTimer();

        // 注册指令
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            CleanupCommands.register(dispatcher, registryAccess, environment);
        });

        // 注册清理工具物品（关键步骤）
        Registry.register(Registries.ITEM, Identifier.of(MOD_ID, "cleanup_hoe"), CLEANUP_HOE);
        LOGGER.info("已注册清理工具: " + Registries.ITEM.getId(CLEANUP_HOE));

        LOGGER.info("ItemCleaner " + MOD_ID + " 初始化完成!");
    }

    public static void setServer(net.minecraft.server.MinecraftServer server) {
        ItemCleaner.server = server;
        if (cleanupTimer != null) {
            cleanupTimer.setServer(server);
        }
    }

    // 保存配置的便捷方法
    public static void saveConfig() {
        if (config != null) {
            CleanupConfig.saveConfig(config);
        }
    }
}