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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // 新增：当前使用的清理列表名称
    public String currentCleanupList = "default";

    // 新增：多清理列表存储（键为列表名，值为物品列表）
    public Map<String, List<String>> cleanupLists = new HashMap<>();

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
        List<String> defaultList = new ArrayList<>();
        defaultList.add("minecraft:cobblestone");
        defaultList.add("minecraft:dirt");
        defaultList.add("minecraft:bone");
        defaultList.add("minecraft:rotten_flesh");
        defaultList.add("minecraft:spider_eye");
        defaultList.add("minecraft:string");
        defaultList.add("minecraft:feather");
        defaultList.add("minecraft:gunpowder");
        defaultList.add("minecraft:flint");
        defaultList.add("minecraft:gravel");
        defaultList.add("minecraft:sand");
        defaultList.add("minecraft:clay_ball");
        defaultList.add("minecraft:snowball");
        defaultList.add("minecraft:egg");
        defaultList.add("minecraft:cod");
        defaultList.add("minecraft:salmon");
        defaultList.add("minecraft:pufferfish");
        defaultList.add("minecraft:tropical_fish");
        defaultList.add("minecraft:beef");
        defaultList.add("minecraft:porkchop");
        defaultList.add("minecraft:chicken");
        defaultList.add("minecraft:rabbit");
        defaultList.add("minecraft:mutton");
        defaultList.add("minecraft:leather");
        defaultList.add("minecraft:wool");
        defaultList.add("minecraft:stick");
        defaultList.add("minecraft:stone");
        defaultList.add("minecraft:cobblestone");
        defaultList.add("minecraft:grass_block");
        defaultList.add("minecraft:dead_bush");
        defaultList.add("minecraft:brown_mushroom");
        defaultList.add("minecraft:red_mushroom");
        defaultList.add("minecraft:lily_pad");
        defaultList.add("minecraft:seagrass");
        defaultList.add("minecraft:kelp");
        defaultConfig.cleanupLists.put("default", defaultList);

        List<String> nowCleanList = new ArrayList<>();
        nowCleanList.add("minecraft:gravel");
        nowCleanList.add("minecraft:sand");
        defaultConfig.cleanupLists.put("NowClean", nowCleanList);

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

    public List<String> getActiveCleanupList() {
        return cleanupLists.getOrDefault(currentCleanupList, new ArrayList<>());
    }

    public boolean switchCleanupList(String listName) {
        if (cleanupLists.containsKey(listName)) {
            currentCleanupList = listName;
            return true;
        }
        return false;
    }

    public boolean createCleanupList(String listName) {
        if (!cleanupLists.containsKey(listName)) {
            cleanupLists.put(listName, new ArrayList<>());
            return true;
        }
        return false;
    }

    public boolean deleteCleanupList(String listName) {
        if (!currentCleanupList.equals(listName) && cleanupLists.containsKey(listName)) {
            cleanupLists.remove(listName);
            return true;
        }
        return false;
    }

    public void addItemToActiveList(String itemId) {
        List<String> activeList = getActiveCleanupList();
        if (!activeList.contains(itemId)) {
            activeList.add(itemId);
        }
    }

    public boolean removeItemFromActiveList(String itemId) {
        List<String> activeList = getActiveCleanupList();
        return activeList.remove(itemId);
    }
}