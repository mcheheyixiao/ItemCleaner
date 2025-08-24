package org.stellarvan.itemcleaner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CleanupConfig {
    // 核心功能开关
    public boolean enableAutoCleanup = true;    // 定时自动清理开关
    public boolean enableThresholdCheck = true;  // 数量阈值检测开关

    // 时间配置（单位：ticks，1秒=20ticks）
    public int cleanupInterval = 12000; // 定时清理间隔（默认10分钟）
    public int thresholdCheckInterval = 100; // 数量检测间隔（默认5秒）
    public int warningCooldown = 1200; // 提示冷却时间（默认5分钟）

    // 范围配置
    public int cleanRadius = 48; // 清理半径（格）
    public int yMin = -64; // Y轴最小范围
    public int yMax = 320; // Y轴最大范围
    public int itemThreshold = 100; // 触发提示的物品数量阈值

    // 语言设置（"zh_cn"=中文, "en_us"=英文）
    public String language = "zh_cn";

    // 需要清理的物品列表
    public List<String> itemsToClean = new ArrayList<>();

    // 加载配置文件
    public static CleanupConfig loadConfig() {
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "itemcleaner.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile, StandardCharsets.UTF_8)) {
                return gson.fromJson(reader, CleanupConfig.class);
            } catch (IOException e) {
                ItemCleaner.LOGGER.error("加载配置文件失败，使用默认配置", e);
            }
        }

        // 初始化默认配置
        CleanupConfig defaultConfig = new CleanupConfig();
        // 添加默认清理物品示例
        defaultConfig.itemsToClean.add("minecraft:cobblestone");
        defaultConfig.itemsToClean.add("minecraft:dirt");
        saveConfig(defaultConfig);
        return defaultConfig;
    }

    // 保存配置文件
    public static void saveConfig(CleanupConfig config) {
        File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "itemcleaner.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter(configFile, StandardCharsets.UTF_8)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            ItemCleaner.LOGGER.error("保存配置文件失败", e);
        }
    }
}
    