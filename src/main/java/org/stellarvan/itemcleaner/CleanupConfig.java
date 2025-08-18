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
    public int cleanupInterval = 12000;
    public String warning1Minute = "§b[服务器娘]§e1分钟后将清理指定掉落物！§r";
    public String warning30Seconds = "§b[服务器娘]§630秒后将清理指定掉落物！§r";
    public String warning5Seconds = "§b[服务器娘]§c5秒后将清理指定掉落物！§r";

    public int cleanRadius = 48;
    public int yMin = -64;
    public int yMax = 320;

    public List<String> itemsToClean = new ArrayList<>();

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

        CleanupConfig defaultConfig = new CleanupConfig();
        saveConfig(defaultConfig);
        return defaultConfig;
    }

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
