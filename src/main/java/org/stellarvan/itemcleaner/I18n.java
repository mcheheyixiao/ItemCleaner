package org.stellarvan.itemcleaner;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class I18n {
    private static final Map<String, String> serverTranslations = new HashMap<>();
    private static String currentLang = "zh_cn";
    private static final Pattern FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[sdf]");

    // 初始化语言系统
    public static void init() {
        if (ItemCleaner.config != null && ItemCleaner.config.language != null) {
            currentLang = ItemCleaner.config.language;
        }
        reloadTranslations();
    }

    // 重新加载翻译
    public static void reloadTranslations() {
        serverTranslations.clear();
        try {
            String langCode = currentLang;

            if ("en_us".equals(langCode)) {
                loadEnglishTranslations();
            } else {
                loadChineseTranslations();
            }

            ItemCleaner.LOGGER.info("[ItemCleaner]{}", String.format(
                    I18n.translate("itemcleaner.log.loading_language"),
                    langCode
            ));

            if (!serverTranslations.containsKey("itemcleaner.prefix")) {
                ItemCleaner.LOGGER.warn("警告：未找到前缀翻译，使用默认值");
                serverTranslations.put("itemcleaner.prefix", "§b[Server]§r ");
            }

            ItemCleaner.LOGGER.info("[ItemCleaner]{}", String.format(
                    I18n.translate("itemcleaner.log.language_loaded"),
                    langCode
            ));
        } catch (Exception e) {
            ItemCleaner.LOGGER.error("[ItemCleaner]语言加载失败", e);
            serverTranslations.put("itemcleaner.prefix", "§b[Server]§r ");
        }
    }

    // 英文翻译
    private static void loadEnglishTranslations() {
        serverTranslations.put("itemcleaner.prefix", "§b[Server]§r "); // 服务器前缀
        serverTranslations.put("itemcleaner.config_saved", "§aConfig saved§r");
        serverTranslations.put("itemcleaner.no_permission", "§cYou don't have permission§r");
        serverTranslations.put("itemcleaner.no_item_held", "§cPlease hold an item§r");
        serverTranslations.put("itemcleaner.item_already_present", "§e%s is already in list§r");
        serverTranslations.put("itemcleaner.language_updated", "§aLanguage updated to %s§r");
        serverTranslations.put("itemcleaner.help_guide", "§eUse /cleandrops help§r");
        serverTranslations.put("itemcleaner.help_title", "§a===== Item Cleanup Help =====");
        serverTranslations.put("itemcleaner.help_command_help", "§b/cleandrops help §7- Show help");
        serverTranslations.put("itemcleaner.help_command_clean", "§b/cleandrops clean §7- Clean items now");
        serverTranslations.put("itemcleaner.help_command_add", "§b/cleandrops add §7- Add held item");
        serverTranslations.put("itemcleaner.help_command_remove", "§b/cleandrops remove <ID> §7- Remove item");
        serverTranslations.put("itemcleaner.help_command_setinterval", "§b/cleandrops setinterval <ticks> §7- Set interval");
        serverTranslations.put("itemcleaner.help_command_list", "§b/cleandrops list §7- Show list");
        serverTranslations.put("itemcleaner.help_command_toggle_auto", "§b/cleandrops toggle auto §7- Toggle auto-clean");
        serverTranslations.put("itemcleaner.help_command_toggle_threshold", "§b/cleandrops toggle threshold §7- Toggle prompts");
        serverTranslations.put("itemcleaner.help_command_language", "§b/cleandrops language <code> §7- Change language");
        serverTranslations.put("itemcleaner.help_note", "§71s = 20ticks, config: itemcleaner.json§r");
        serverTranslations.put("itemcleaner.cleanup_start", "§aStarting cleanup...§r");
        serverTranslations.put("itemcleaner.cleanup_completed", "§aCleaned %d items§r");
        serverTranslations.put("itemcleaner.cleanup_none", "§aNo items to clean§r");
        serverTranslations.put("itemcleaner.threshold_prompt", "§eFound %d items. Clean now? ");
        serverTranslations.put("itemcleaner.button_yes", "[Yes]");
        serverTranslations.put("itemcleaner.button_no", "[No]");
        serverTranslations.put("itemcleaner.choice_yes_feedback", "§aCleanup executed§r");
        serverTranslations.put("itemcleaner.choice_no_feedback", "§eScheduled cleanup remains§r");
        serverTranslations.put("itemcleaner.warning_1min", "§eCleanup in 1 minute§r");
        serverTranslations.put("itemcleaner.warning_30s", "§eCleanup in 30 seconds§r");
        serverTranslations.put("itemcleaner.warning_5s", "§eCleanup in 5 seconds§r");
        serverTranslations.put("itemcleaner.toggle_auto_enabled", "§aAuto-clean enabled§r");
        serverTranslations.put("itemcleaner.toggle_auto_disabled", "§cAuto-clean disabled§r");
        serverTranslations.put("itemcleaner.toggle_threshold_enabled", "§aThreshold enabled§r");
        serverTranslations.put("itemcleaner.toggle_threshold_disabled", "§cThreshold disabled§r");
        serverTranslations.put("itemcleaner.item_added", "§aAdded %s to list§r");
        serverTranslations.put("itemcleaner.item_removed", "§aRemoved %s from list§r");
        serverTranslations.put("itemcleaner.item_not_found", "§c%s not in list§r");
        serverTranslations.put("itemcleaner.interval_updated", "§aInterval set to %d ticks§r");
        serverTranslations.put("itemcleaner.interval_invalid", "§cMinimum 200 ticks§r");
        serverTranslations.put("itemcleaner.list_title", "§a===== Cleanup List =====");
        serverTranslations.put("itemcleaner.list_empty", "§eList is empty§r");
        serverTranslations.put("itemcleaner.log.mod_loaded", "Mod loaded");
        serverTranslations.put("itemcleaner.log.server_instance_set", "Server instance set");
        serverTranslations.put("itemcleaner.log.cleanup_config", "Current cleanup config - Enabled: %b, Warning cooldown: %d seconds (%d ticks)");
        serverTranslations.put("itemcleaner.log.threshold_config", "Threshold check config - Enabled: %b, Check interval: %d ticks, Trigger threshold: %d, Prompt interval: %d");
        serverTranslations.put("itemcleaner.log.loading_language", "Loading language: %s");
        serverTranslations.put("itemcleaner.log.language_loaded", "Language loaded: %s");
        serverTranslations.put("item.itemcleaner.cleanup_hoe","clenaup_hoe");
        serverTranslations.put("itemcleaner.log.cleanup_range","Cleanup range - Detection radius: %d blocks");
        serverTranslations.put("itemcleaner.list_title_current","§a===== Cleanup List: %s =====");
        serverTranslations.put("itemcleaner.list_title_specific","§a===== List: %s =====");
        serverTranslations.put("itemcleaner.list_not_found","§cList %s not found§r");
        serverTranslations.put("itemcleaner.list_switched","§aSwitched to cleanup list: %s§r");
        serverTranslations.put("itemcleaner.list_created","§aCreated new cleanup list: %s§r");
        serverTranslations.put("itemcleaner.list_already_exists","§eCleanup list %s already exists§r");
        serverTranslations.put("itemcleaner.list_deleted","§aDeleted cleanup list: %s§r");
        serverTranslations.put("itemcleaner.list_cannot_delete_current","§cCannot delete current active list: %s§r");
        serverTranslations.put("itemcleaner.current_list","§aCurrent active cleanup list: %s§r");
        serverTranslations.put("itemcleaner.all_lists_title","§a===== All Cleanup Lists =====");
        serverTranslations.put("itemcleaner.no_lists_found","§eNo cleanup lists found§r");
        serverTranslations.put("itemcleaner.help_command_listmanager_switch","§b/cleandrops listmanager switch <name> §7- Switch active cleanup list");
        serverTranslations.put("itemcleaner.help_command_listmanager_create","§b/cleandrops listmanager create <name> §7- Create new cleanup list");
        serverTranslations.put("itemcleaner.help_command_listmanager_delete","§b/cleandrops listmanager delete <name> §7- Delete cleanup list");
        serverTranslations.put("itemcleaner.help_command_listmanager_current","§b/cleandrops listmanager current §7- Show current active list");
        serverTranslations.put("itemcleaner.help_command_listmanager_all","§b/cleandrops listmanager all §7- Show all available lists");
        serverTranslations.put("itemcleaner.available_lists_prompt","§aAvailable cleanup lists:");
        serverTranslations.put("itemcleaner.all_lists_with_items_title","§a===== All Lists and Items =====");
        serverTranslations.put("itemcleaner.help_command_listmanager_list", "§b/cleandrops listmanager list §7- View all lists and their items");
        serverTranslations.put("itemcleaner.invalid_list_name", "Invalid list name! Name cannot contain spaces or be empty.");
        serverTranslations.put("itemcleaner.cannot_delete_last_list", "Cannot delete the last list! At least one list must be kept.");
    }

    // 中文翻译
    private static void loadChineseTranslations() {
        serverTranslations.put("itemcleaner.prefix", "§b[服务器娘]§r "); // 服务器前缀
        serverTranslations.put("itemcleaner.config_saved", "§a配置已保存§r");
        serverTranslations.put("itemcleaner.no_permission", "§c没有权限执行此命令§r");
        serverTranslations.put("itemcleaner.no_item_held", "§c请手持物品执行§r");
        serverTranslations.put("itemcleaner.item_already_present", "§e%s 已在清理列表中§r");
        serverTranslations.put("itemcleaner.language_updated", "§a语言已更新为 %s§r");
        serverTranslations.put("itemcleaner.help_guide", "§e使用 /cleandrops help 查看命令§r");
        serverTranslations.put("itemcleaner.help_title", "§a===== 掉落物清理命令帮助 =====");
        serverTranslations.put("itemcleaner.help_command_help", "§b/cleandrops help §7- 显示帮助");
        serverTranslations.put("itemcleaner.help_command_clean", "§b/cleandrops clean §7- 立即清理");
        serverTranslations.put("itemcleaner.help_command_add", "§b/cleandrops add §7- 添加手持物品");
        serverTranslations.put("itemcleaner.help_command_remove", "§b/cleandrops remove <ID> §7- 移除物品");
        serverTranslations.put("itemcleaner.help_command_setinterval", "§b/cleandrops setinterval <ticks> §7- 设置间隔");
        serverTranslations.put("itemcleaner.help_command_list", "§b/cleandrops list §7- 显示列表");
        serverTranslations.put("itemcleaner.help_command_toggle_auto", "§b/cleandrops toggle auto §7- 开关自动清理");
        serverTranslations.put("itemcleaner.help_command_toggle_threshold", "§b/cleandrops toggle threshold §7- 开关提示");
        serverTranslations.put("itemcleaner.help_command_language", "§b/cleandrops language <代码> §7- 切换语言");
        serverTranslations.put("itemcleaner.help_note", "§71秒=20游戏刻，配置文件: itemcleaner.json§r");
        serverTranslations.put("itemcleaner.cleanup_start", "§a开始清理...§r");
        serverTranslations.put("itemcleaner.cleanup_completed", "§a共清理 %d 个物品§r");
        serverTranslations.put("itemcleaner.cleanup_none", "§a没有可清理的物品§r");
        serverTranslations.put("itemcleaner.threshold_prompt", "§e检测到 %d 个物品，立即清理？ ");
        serverTranslations.put("itemcleaner.button_yes", "[确认]");
        serverTranslations.put("itemcleaner.button_no", "[取消]");
        serverTranslations.put("itemcleaner.choice_yes_feedback", "§a已执行清理§r");
        serverTranslations.put("itemcleaner.choice_no_feedback", "§e将按计划清理§r");
        serverTranslations.put("itemcleaner.warning_1min", "§e1分钟后执行清理§r");
        serverTranslations.put("itemcleaner.warning_30s", "§e30秒后执行清理§r");
        serverTranslations.put("itemcleaner.warning_5s", "§e5秒后执行清理§r");
        serverTranslations.put("itemcleaner.toggle_auto_enabled", "§a自动清理已开启§r");
        serverTranslations.put("itemcleaner.toggle_auto_disabled", "§c自动清理已关闭§r");
        serverTranslations.put("itemcleaner.toggle_threshold_enabled", "§a阈值检测已开启§r");
        serverTranslations.put("itemcleaner.toggle_threshold_disabled", "§c阈值检测已关闭§r");
        serverTranslations.put("itemcleaner.item_added", "§a已添加 %s 到列表§r");
        serverTranslations.put("itemcleaner.item_removed", "§a已从列表移除 %s§r");
        serverTranslations.put("itemcleaner.item_not_found", "§c列表中没有 %s§r");
        serverTranslations.put("itemcleaner.interval_updated", "§a间隔已设为 %d 游戏刻§r");
        serverTranslations.put("itemcleaner.interval_invalid", "§c最低200游戏刻§r");
        serverTranslations.put("itemcleaner.list_title", "§a===== 清理列表 =====");
        serverTranslations.put("itemcleaner.list_empty", "§e列表为空§r");
        serverTranslations.put("itemcleaner.log.mod_loaded", "模组已加载");
        serverTranslations.put("itemcleaner.log.server_instance_set", "服务器实例已设置");
        serverTranslations.put("itemcleaner.log.cleanup_config", "当前清理配置 - 是否启用: %b, 提示冷却时间: %d秒 (%d ticks)");
        serverTranslations.put("itemcleaner.log.threshold_config", "阈值检测配置 - 是否启用: %b, 检测间隔: %d ticks, 触发阈值: %d, 提示间隔: %d");
        serverTranslations.put("itemcleaner.log.loading_language", "加载语言: %s");
        serverTranslations.put("itemcleaner.log.language_loaded", "语言加载完成: %s");
        serverTranslations.put("item.itemcleaner.cleanup_hoe","清理之锄");
        serverTranslations.put("itemcleaner.log.cleanup_range","清理范围 - 检测范围: %d 格(半径)");
        serverTranslations.put("itemcleaner.list_title_current","§a===== 清理列表: %s =====");
        serverTranslations.put("itemcleaner.list_title_specific","§a===== 列表: %s =====");
        serverTranslations.put("itemcleaner.list_not_found","§c未找到列表 %s§r");
        serverTranslations.put("itemcleaner.list_switched","§a已切换到清理列表: %s§r");
        serverTranslations.put("itemcleaner.list_created","§a已创建新清理列表: %s§r");
        serverTranslations.put("itemcleaner.list_already_exists","§e清理列表 %s 已存在§r");
        serverTranslations.put("itemcleaner.list_deleted","§a已删除清理列表: %s§r");
        serverTranslations.put("itemcleaner.list_cannot_delete_current","§c无法删除当前正在使用的列表: %s§r");
        serverTranslations.put("itemcleaner.current_list","§a当前活跃的清理列表: %s§r");
        serverTranslations.put("itemcleaner.all_lists_title","§a===== 所有清理列表 =====");
        serverTranslations.put("itemcleaner.no_lists_found","§e未找到任何清理列表§r");
        serverTranslations.put("itemcleaner.help_command_listmanager_switch","§b/cleandrops listmanager switch <名称> §7- 切换活跃的清理列表");
        serverTranslations.put("itemcleaner.help_command_listmanager_create","§b/cleandrops listmanager create <名称> §7- 创建新的清理列表");
        serverTranslations.put("itemcleaner.help_command_listmanager_delete","§b/cleandrops listmanager delete <名称> §7- 删除清理列表");
        serverTranslations.put("itemcleaner.help_command_listmanager_current","§b/cleandrops listmanager current §7- 显示当前活跃的列表");
        serverTranslations.put("itemcleaner.help_command_listmanager_all","§b/cleandrops listmanager all §7- 显示所有可用列表");
        serverTranslations.put("itemcleaner.available_lists_prompt","§a可用的清理列表：");
        serverTranslations.put("itemcleaner.all_lists_with_items_title","§a===== 所有列表及物品 =====");
        serverTranslations.put("itemcleaner.help_command_listmanager_list", "§b/cleandrops listmanager list §7- 查看所有列表及包含的物品");
        serverTranslations.put("itemcleaner.invalid_list_name", "无效的列表名称！名称不能包含空格或为空。");
        serverTranslations.put("itemcleaner.cannot_delete_last_list", "无法删除最后一个列表！至少需要保留一个列表。");
    }

    // 获取翻译文本
    public static String translate(String key) {
        // 客户端使用内置翻译
        if (isClientSide() && Language.getInstance().hasTranslation(key)) {
            return Language.getInstance().get(key);
        }

        // 服务器使用手动映射，确保前缀一定会被返回
        if (serverTranslations.containsKey(key)) {
            return serverTranslations.get(key);
        }

        // 前缀特殊处理：如果丢失则返回默认前缀
        if ("itemcleaner.prefix".equals(key)) {
            return "§b[Server]§r ";
        }

        ItemCleaner.LOGGER.warn("[ItemCleaner]缺少翻译键: " + key);
        return key;
    }

    // 带参数的翻译
    public static String translate(String key, Object... args) {
        String translation = translate(key);
        try {
            Matcher matcher = FORMAT_PATTERN.matcher(translation);
            StringBuffer result = new StringBuffer();
            int argIndex = 0;

            while (matcher.find() && argIndex < args.length) {
                matcher.appendReplacement(result, Matcher.quoteReplacement(args[argIndex++].toString()));
            }
            matcher.appendTail(result);
            return result.toString();
        } catch (Exception e) {
            return translation;
        }
    }

    // 构建带前缀的文本（重点修复：确保前缀正确拼接）
    public static MutableText prefixedText(String key) {
        String prefix = translate("itemcleaner.prefix");
        String content = translate(key);
        return Text.literal(prefix).append(Text.literal(content));
    }

    public static MutableText prefixedText(String key, Object... args) {
        String prefix = translate("itemcleaner.prefix");
        String content = translate(key, args);
        return Text.literal(prefix).append(Text.literal(content));
    }

    // 其他文本构建方法
    public static MutableText text(String key) {
        return Text.literal(translate(key));
    }

    public static MutableText text(String key, Object... args) {
        return Text.literal(translate(key, args));
    }

    // 环境判断
    private static boolean isClientSide() {
        try {
            Class.forName("net.minecraft.client.MinecraftClient");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // 语言切换
    public static void setLanguage(String langCode) {
        currentLang = langCode;
        reloadTranslations();
    }

}