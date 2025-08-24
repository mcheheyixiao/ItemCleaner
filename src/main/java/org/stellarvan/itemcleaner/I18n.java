package org.stellarvan.itemcleaner;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class I18n {
    // 服务器端翻译映射（必须包含所有翻译键）
    private static final Map<String, String> serverTranslations = new HashMap<>();
    private static String currentLang = "zh_cn";
    private static final Pattern FORMAT_PATTERN = Pattern.compile("%(\\d+\\$)?[sdf]");

    /**
     * 初始化语言系统
     */
    public static void init() {
        if (ItemCleaner.config != null && ItemCleaner.config.language != null) {
            currentLang = ItemCleaner.config.language;
        }
        reloadTranslations();
    }

    /**
     * 重新加载翻译（核心修复：完善服务器端映射）
     */
    public static void reloadTranslations() {
        serverTranslations.clear();
        try {
            String langCode = currentLang;
            ItemCleaner.LOGGER.info("Loading language: " + langCode);

            // 服务器端手动加载所有翻译键（与语言文件保持一致）
            if ("en_us".equals(langCode)) {
                loadEnglishTranslations();
            } else { // 默认中文
                loadChineseTranslations();
            }

            ItemCleaner.LOGGER.info("Successfully loaded language: " + langCode);
        } catch (Exception e) {
            ItemCleaner.LOGGER.error("Failed to load translations", e);
            currentLang = "zh_cn";
            loadChineseTranslations(); // 失败时默认加载中文
        }
    }

    /**
     * 加载英文翻译（服务器端）
     */
    private static void loadEnglishTranslations() {
        serverTranslations.put("itemcleaner.prefix", "§b[Server]§r ");
        serverTranslations.put("itemcleaner.config_saved", "§aConfig saved§r");
        serverTranslations.put("itemcleaner.no_permission", "§cYou don't have permission to execute this command§r");
        serverTranslations.put("itemcleaner.no_item_held", "§cPlease hold an item to execute this command§r");
        serverTranslations.put("itemcleaner.item_already_present", "§e%s is already in the cleanup list§r");
        serverTranslations.put("itemcleaner.language_updated", "§aLanguage updated to %s§r");
        serverTranslations.put("itemcleaner.help_guide", "§eUse /cleandrops help to view all commands§r");
        serverTranslations.put("itemcleaner.help_title", "§a===== Item Cleanup Help =====");
        serverTranslations.put("itemcleaner.help_command_help", "§b/cleandrops help §7- Show this help message");
        serverTranslations.put("itemcleaner.help_command_clean", "§b/cleandrops clean §7- Clean specified items immediately");
        serverTranslations.put("itemcleaner.help_command_add", "§b/cleandrops add §7- Add held item to cleanup list");
        serverTranslations.put("itemcleaner.help_command_remove", "§b/cleandrops remove <itemID> §7- Remove item from cleanup list");
        serverTranslations.put("itemcleaner.help_command_setinterval", "§b/cleandrops setinterval <ticks> §7- Set auto-clean interval (min 200ticks)");
        serverTranslations.put("itemcleaner.help_command_list", "§b/cleandrops list §7- View items in cleanup list");
        serverTranslations.put("itemcleaner.help_command_toggle_auto", "§b/cleandrops toggle auto §7- Toggle auto-cleanup");
        serverTranslations.put("itemcleaner.help_command_toggle_threshold", "§b/cleandrops toggle threshold §7- Toggle threshold prompts");
        serverTranslations.put("itemcleaner.help_command_language", "§b/cleandrops language <code> §7- Change language (e.g. en_us, zh_cn)");
        serverTranslations.put("itemcleaner.help_note", "§7Note: 1 second = 20ticks, config at .minecraft/config/itemcleaner.json§r");
        serverTranslations.put("itemcleaner.cleanup_start", "§aStarting cleanup...§r");
        serverTranslations.put("itemcleaner.cleanup_completed", "§aCleanup result: Total %d items cleaned§r");
        serverTranslations.put("itemcleaner.cleanup_none", "§aNo items found to clean§r");
        serverTranslations.put("itemcleaner.threshold_prompt", "§eDetected %d specified items. Clean immediately? ");
        serverTranslations.put("itemcleaner.button_yes", "[Yes]");
        serverTranslations.put("itemcleaner.button_no", "[No]");
        serverTranslations.put("itemcleaner.choice_yes_feedback", "§aImmediate cleanup executed§r");
        serverTranslations.put("itemcleaner.choice_no_feedback", "§eWill perform scheduled cleanup§r");
        serverTranslations.put("itemcleaner.warning_1min", "§eItem cleanup in 1 minute§r");
        serverTranslations.put("itemcleaner.warning_30s", "§eItem cleanup in 30 seconds§r");
        serverTranslations.put("itemcleaner.warning_5s", "§eItem cleanup in 5 seconds§r");
        serverTranslations.put("itemcleaner.toggle_auto_enabled", "§aAuto-cleanup enabled§r");
        serverTranslations.put("itemcleaner.toggle_auto_disabled", "§cAuto-cleanup disabled§r");
        serverTranslations.put("itemcleaner.toggle_threshold_enabled", "§aThreshold check enabled§r");
        serverTranslations.put("itemcleaner.toggle_threshold_disabled", "§cThreshold check disabled§r");
        serverTranslations.put("itemcleaner.item_added", "§aAdded %s to cleanup list§r");
        serverTranslations.put("itemcleaner.item_removed", "§aRemoved %s from cleanup list§r");
        serverTranslations.put("itemcleaner.item_not_found", "§c%s not found in cleanup list§r");
        serverTranslations.put("itemcleaner.interval_updated", "§aAuto-clean interval updated to %d ticks§r");
        serverTranslations.put("itemcleaner.interval_invalid", "§cInvalid interval (minimum 200 ticks)§r");
        serverTranslations.put("itemcleaner.list_title", "§a===== Cleanup List =====");
        serverTranslations.put("itemcleaner.list_empty", "§eCleanup list is empty§r");
    }

    /**
     * 加载中文翻译（服务器端）
     */
    private static void loadChineseTranslations() {
        serverTranslations.put("itemcleaner.prefix", "§b[服务器娘]§r ");
        serverTranslations.put("itemcleaner.config_saved", "§a配置已保存§r");
        serverTranslations.put("itemcleaner.no_permission", "§c你没有权限执行此命令§r");
        serverTranslations.put("itemcleaner.no_item_held", "§c请手持物品执行此命令§r");
        serverTranslations.put("itemcleaner.item_already_present", "§e%s 已在清理列表中§r");
        serverTranslations.put("itemcleaner.language_updated", "§a语言已更新为 %s§r");
        serverTranslations.put("itemcleaner.help_guide", "§e使用 /cleandrops help 查看所有命令§r");
        serverTranslations.put("itemcleaner.help_title", "§a===== 掉落物清理命令帮助 =====");
        serverTranslations.put("itemcleaner.help_command_help", "§b/cleandrops help §7- 显示本帮助信息");
        serverTranslations.put("itemcleaner.help_command_clean", "§b/cleandrops clean §7- 立即清理指定物品");
        serverTranslations.put("itemcleaner.help_command_add", "§b/cleandrops add §7- 将手持物品添加到清理列表");
        serverTranslations.put("itemcleaner.help_command_remove", "§b/cleandrops remove <物品ID> §7- 从清理列表移除物品");
        serverTranslations.put("itemcleaner.help_command_setinterval", "§b/cleandrops setinterval <ticks> §7- 设置自动清理间隔（最低200ticks）");
        serverTranslations.put("itemcleaner.help_command_list", "§b/cleandrops list §7- 查看清理列表中的物品");
        serverTranslations.put("itemcleaner.help_command_toggle_auto", "§b/cleandrops toggle auto §7- 开关自动清理功能");
        serverTranslations.put("itemcleaner.help_command_toggle_threshold", "§b/cleandrops toggle threshold §7- 开关数量阈值提示");
        serverTranslations.put("itemcleaner.help_command_language", "§b/cleandrops language <代码> §7- 切换语言（例如 en_us, zh_cn）");
        serverTranslations.put("itemcleaner.help_note", "§7注：1秒 = 20ticks，配置文件位于 .minecraft/config/itemcleaner.json§r");
        serverTranslations.put("itemcleaner.cleanup_start", "§a开始执行清理...§r");
        serverTranslations.put("itemcleaner.cleanup_completed", "§a本次清理结果: 共清理 %d 个物品§r");
        serverTranslations.put("itemcleaner.cleanup_none", "§a未发现需要清理的物品§r");
        serverTranslations.put("itemcleaner.threshold_prompt", "§e检测到指定物品已达 %d 个，是否立即清理？ ");
        serverTranslations.put("itemcleaner.button_yes", "[确认清理]");
        serverTranslations.put("itemcleaner.button_no", "[稍后清理]");
        serverTranslations.put("itemcleaner.choice_yes_feedback", "§a已执行立即清理§r");
        serverTranslations.put("itemcleaner.choice_no_feedback", "§e将按计划执行定时清理§r");
        serverTranslations.put("itemcleaner.warning_1min", "§e物品清理将在1分钟后执行§r");
        serverTranslations.put("itemcleaner.warning_30s", "§e物品清理将在30秒后执行§r");
        serverTranslations.put("itemcleaner.warning_5s", "§e物品清理将在5秒后执行§r");
        serverTranslations.put("itemcleaner.toggle_auto_enabled", "§a自动清理已开启§r");
        serverTranslations.put("itemcleaner.toggle_auto_disabled", "§c自动清理已关闭§r");
        serverTranslations.put("itemcleaner.toggle_threshold_enabled", "§a阈值检测已开启§r");
        serverTranslations.put("itemcleaner.toggle_threshold_disabled", "§c阈值检测已关闭§r");
        serverTranslations.put("itemcleaner.item_added", "§a已将 %s 添加到清理列表§r");
        serverTranslations.put("itemcleaner.item_removed", "§a已从清理列表移除 %s§r");
        serverTranslations.put("itemcleaner.item_not_found", "§c未在清理列表中找到 %s§r");
        serverTranslations.put("itemcleaner.interval_updated", "§a自动清理间隔已更新为 %d ticks§r");
        serverTranslations.put("itemcleaner.interval_invalid", "§c无效的间隔值（最低200 ticks）§r");
        serverTranslations.put("itemcleaner.list_title", "§a===== 清理列表 =====");
        serverTranslations.put("itemcleaner.list_empty", "§e清理列表为空§r");
    }

    /**
     * 获取翻译文本（兼容客户端和服务器）
     */
    public static String translate(String key) {
        // 客户端使用内置语言系统
        if (isClientSide() && Language.getInstance().hasTranslation(key)) {
            return Language.getInstance().get(key);
        }

        // 服务器使用手动映射
        if (serverTranslations.containsKey(key)) {
            return serverTranslations.get(key);
        }

        // 未找到翻译时记录警告
        ItemCleaner.LOGGER.warn("Missing translation for key: " + key);
        return key;
    }

    /**
     * 带参数的翻译
     */
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

    /**
     * 判断是否为客户端环境
     */
    private static boolean isClientSide() {
        try {
            Class.forName("net.minecraft.client.MinecraftClient");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    // 以下方法保持不变（文本构建相关）
    public static MutableText prefixedText(String key) {
        return Text.literal(translate("itemcleaner.prefix")).append(Text.literal(translate(key)));
    }

    public static MutableText prefixedText(String key, Object... args) {
        return Text.literal(translate("itemcleaner.prefix")).append(Text.literal(translate(key, args)));
    }

    public static MutableText text(String key) {
        return Text.literal(translate(key));
    }

    public static MutableText text(String key, Object... args) {
        return Text.literal(translate(key, args));
    }

    public static void setLanguage(String langCode) {
        currentLang = langCode;
        reloadTranslations();
    }
}
