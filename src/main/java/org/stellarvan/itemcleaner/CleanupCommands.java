package org.stellarvan.itemcleaner;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.stellarvan.itemcleaner.ItemCleaner.MOD_ID;

public class CleanupCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("cleandrops")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    context.getSource().sendFeedback(
                            () -> I18n.prefixedText("itemcleaner.help_guide"),
                            false
                    );
                    return 1;
                })
                .then(CommandManager.literal("help")
                        .executes(CleanupCommands::showHelp))
                .then(CommandManager.literal("clean")
                        .executes(CleanupCommands::executeCleanup))
                .then(CommandManager.literal("add")
                        .executes(CleanupCommands::addHeldItem))
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("itemId", StringArgumentType.string())
                                .executes(CleanupCommands::removeItem)))
                .then(CommandManager.literal("setinterval")
                        .then(CommandManager.argument("ticks", IntegerArgumentType.integer(200))
                                .executes(CleanupCommands::setInterval)))
                .then(CommandManager.literal("list")
                        .executes(CleanupCommands::listItems)
                        .then(CommandManager.argument("listName", StringArgumentType.string())
                                .executes(CleanupCommands::listSpecificList)))
                .then(CommandManager.literal("toggle")
                        .then(CommandManager.literal("auto")
                                .executes(CleanupCommands::toggleAutoCleanup))
                        .then(CommandManager.literal("threshold")
                                .executes(CleanupCommands::toggleThresholdCheck)))
                .then(CommandManager.literal("confirm")
                        .then(CommandManager.argument("requestId", StringArgumentType.string())
                                .then(CommandManager.argument("choice", StringArgumentType.string())
                                        .executes(context -> InteractiveCleanupHandler.handleConfirmation(context)))))
                .then(CommandManager.literal("language")
                        .then(CommandManager.argument("langCode", StringArgumentType.string())
                                .executes(CleanupCommands::setLanguage)))
                // 列表管理命令
                .then(CommandManager.literal("listmanager")
                        .then(CommandManager.literal("switch")
                                .executes(CleanupCommands::showAvailableLists)
                                .then(CommandManager.argument("listName", StringArgumentType.string())
                                        .executes(CleanupCommands::switchCleanupList)))
                        .then(CommandManager.literal("create")
                                .then(CommandManager.argument("listName", StringArgumentType.string())
                                        .executes(CleanupCommands::createCleanupList)))
                        .then(CommandManager.literal("delete")
                                .then(CommandManager.argument("listName", StringArgumentType.string())
                                        .executes(CleanupCommands::deleteCleanupList)))
                        .then(CommandManager.literal("current")
                                .executes(CleanupCommands::showCurrentList))
                        .then(CommandManager.literal("all")
                                .executes(CleanupCommands::listAllLists))
                        .then(CommandManager.literal("list")
                                .executes(CleanupCommands::listAllListsWithItems))));
    }

    // 语言切换命令
    private static int setLanguage(CommandContext<ServerCommandSource> context) {
        String langCode = StringArgumentType.getString(context, "langCode");
        ItemCleaner.config.language = langCode;
        ItemCleaner.saveConfig();
        I18n.setLanguage(langCode);

        context.getSource().sendFeedback(
                () -> I18n.prefixedText("itemcleaner.language_updated", langCode),
                false
        );
        return 1;
    }

    // 查看所有列表及包含的物品
    private static int listAllListsWithItems(CommandContext<ServerCommandSource> context) {
        Map<String, List<String>> allLists = ItemCleaner.config.cleanupLists;

        if (allLists.isEmpty()) {
            context.getSource().sendFeedback(
                    () -> I18n.prefixedText("itemcleaner.no_lists_found"),
                    false
            );
            return 0;
        }

        context.getSource().sendFeedback(
                () -> I18n.text("itemcleaner.all_lists_with_items_title"),
                false
        );

        for (Map.Entry<String, List<String>> entry : allLists.entrySet()) {
            String listName = entry.getKey();
            List<String> items = entry.getValue();

            String titlePrefix = (ItemCleaner.config.currentCleanupList.equals(listName)) ? "§a* " : "§6";
            context.getSource().sendFeedback(
                    () -> Text.literal(titlePrefix + "===== " + listName + " ====="),
                    false
            );

            if (items.isEmpty()) {
                context.getSource().sendFeedback(
                        () -> Text.literal("§7  该列表为空§r"),
                        false
                );
            } else {
                for (String itemId : items) {
                    String itemName = "未知物品";
                    try {
                        Identifier id = Identifier.of(MOD_ID, itemId);
                        if (Registries.ITEM.containsId(id)) {
                            itemName = Registries.ITEM.get(id).getName().getString();
                        }
                    } catch (Exception e) {
                        // 忽略无效ID的错误
                    }
                    String finalItemName = itemName;
                    context.getSource().sendFeedback(
                            () -> Text.literal("§7- " + finalItemName + " §8(" + itemId + ")§r"),
                            false
                    );
                }
            }
        }

        context.getSource().sendFeedback(
                () -> Text.literal("§7* 表示当前使用的列表§r"),
                false
        );
        return 1;
    }

    // 无参数时显示可用列表（供switch指令使用）
    private static int showAvailableLists(CommandContext<ServerCommandSource> context) {
        Set<String> listNames = ItemCleaner.config.cleanupLists.keySet();

        if (listNames.isEmpty()) {
            context.getSource().sendFeedback(
                    () -> I18n.prefixedText("itemcleaner.no_lists_found"),
                    false
            );
            return 0;
        }

        context.getSource().sendFeedback(
                () -> Text.translatable("itemcleaner.available_lists_prompt"),
                false
        );

        for (String listName : listNames) {
            final String currentListName = listName;
            String prefix = (ItemCleaner.config.currentCleanupList.equals(currentListName)) ? "§a* " : "§7- ";

            context.getSource().sendFeedback(
                    () -> Text.literal(prefix + currentListName + " §7(" +
                            ItemCleaner.config.cleanupLists.get(currentListName).size() + "个物品)"),
                    false
            );
        }

        context.getSource().sendFeedback(
                () -> Text.literal("§7使用 /cleandrops listmanager switch <列表名> 切换§r"),
                false
        );
        return 1;
    }

    // 帮助命令
    private static int showHelp(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        source.sendFeedback(() -> Text.translatable("itemcleaner.help_title"), false);

        // 基础命令组
        source.sendFeedback(() -> Text.translatable("itemcleaner.help_command_help"), false);
        source.sendFeedback(() -> Text.translatable("itemcleaner.help_command_clean"), false);
        source.sendFeedback(() -> Text.translatable("itemcleaner.help_command_add"), false);
        source.sendFeedback(() -> Text.translatable("itemcleaner.help_command_remove"), false);
        source.sendFeedback(() -> Text.translatable("itemcleaner.help_command_setinterval"), false);
        source.sendFeedback(() -> Text.translatable("itemcleaner.help_command_list"), false);

        // 切换命令组
        source.sendFeedback(() -> Text.translatable("itemcleaner.help_command_toggle_auto"), false);
        source.sendFeedback(() -> Text.translatable("itemcleaner.help_command_toggle_threshold"), false);

        // 语言命令
        source.sendFeedback(() -> Text.translatable("itemcleaner.help_command_language"), false);

        // 列表管理命令组
        source.sendFeedback(() -> Text.translatable("itemcleaner.help_command_listmanager_switch"), false);
        source.sendFeedback(() -> Text.translatable("itemcleaner.help_command_listmanager_create"), false);
        source.sendFeedback(() -> Text.translatable("itemcleaner.help_command_listmanager_delete"), false);
        source.sendFeedback(() -> Text.translatable("itemcleaner.help_command_listmanager_current"), false);
        source.sendFeedback(() -> Text.translatable("itemcleaner.help_command_listmanager_all"), false);
        source.sendFeedback(() -> Text.translatable("itemcleaner.help_command_listmanager_list"), false);

        // 备注信息
        source.sendFeedback(() -> Text.translatable("itemcleaner.help_note"), false);
        source.sendFeedback(() -> Text.translatable("itemcleaner.help_title"), false);

        return 1;
    }

    // 立即清理
    private static int executeCleanup(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(
                () -> I18n.prefixedText("itemcleaner.cleanup_start"),
                true
        );
        ItemCleaner.cleanupTimer.performCleanup(true);
        return 1;
    }

    // 添加物品
    private static int addHeldItem(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        ItemStack heldStack = player.getMainHandStack();

        if (heldStack.isEmpty()) {
            context.getSource().sendFeedback(
                    () -> I18n.prefixedText("itemcleaner.no_item_held"),
                    false
            );
            return 0;
        }

        Identifier itemId = Registries.ITEM.getId(heldStack.getItem());
        String itemIdStr = itemId.toString();

        List<String> activeList = ItemCleaner.config.getActiveCleanupList();
        if (!activeList.contains(itemIdStr)) {
            ItemCleaner.config.addItemToActiveList(itemIdStr);
            CleanupConfig.saveConfig(ItemCleaner.config);

            context.getSource().sendFeedback(
                    () -> I18n.prefixedText("itemcleaner.item_added", itemIdStr),
                    false
            );
        } else {
            context.getSource().sendFeedback(
                    () -> I18n.prefixedText("itemcleaner.item_already_present", itemIdStr),
                    false
            );
        }

        return 1;
    }

    // 移除物品
    private static int removeItem(CommandContext<ServerCommandSource> context) {
        String itemId = StringArgumentType.getString(context, "itemId");

        if (ItemCleaner.config.removeItemFromActiveList(itemId)) {
            CleanupConfig.saveConfig(ItemCleaner.config);
            context.getSource().sendFeedback(
                    () -> I18n.prefixedText("itemcleaner.item_removed", itemId),
                    false
            );
        } else {
            context.getSource().sendFeedback(
                    () -> I18n.prefixedText("itemcleaner.item_not_found", itemId),
                    false
            );
        }

        return 1;
    }

    // 设置间隔
    private static int setInterval(CommandContext<ServerCommandSource> context) {
        int ticks = IntegerArgumentType.getInteger(context, "ticks");
        ItemCleaner.config.cleanupInterval = ticks;
        CleanupConfig.saveConfig(ItemCleaner.config);

        context.getSource().sendFeedback(
                () -> I18n.prefixedText("itemcleaner.interval_updated", ticks),
                false
        );
        return 1;
    }

    // 列出当前列表物品
    private static int listItems(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(
                () -> Text.translatable("itemcleaner.list_title_current", ItemCleaner.config.currentCleanupList),
                false
        );

        List<String> activeList = ItemCleaner.config.getActiveCleanupList();
        if (activeList.isEmpty()) {
            context.getSource().sendFeedback(
                    () -> Text.translatable("itemcleaner.list_empty"),
                    false
            );
            return 1;
        }

        for (String itemId : activeList) {
            context.getSource().sendFeedback(
                    () -> Text.literal("§7- " + itemId),
                    false
            );
        }

        return 1;
    }

    // 列出指定列表物品
    private static int listSpecificList(CommandContext<ServerCommandSource> context) {
        String listName = StringArgumentType.getString(context, "listName");
        List<String> targetList = ItemCleaner.config.cleanupLists.get(listName);

        if (targetList == null) {
            context.getSource().sendFeedback(
                    () -> I18n.prefixedText("itemcleaner.list_not_found", listName),
                    false
            );
            return 0;
        }

        context.getSource().sendFeedback(
                () -> Text.translatable("itemcleaner.list_title_specific", listName),
                false
        );

        if (targetList.isEmpty()) {
            context.getSource().sendFeedback(
                    () -> Text.translatable("itemcleaner.list_empty"),
                    false
            );
            return 1;
        }

        for (String itemId : targetList) {
            context.getSource().sendFeedback(
                    () -> Text.literal("§7- " + itemId),
                    false
            );
        }

        return 1;
    }

    // 切换自动清理
    private static int toggleAutoCleanup(CommandContext<ServerCommandSource> context) {
        ItemCleaner.config.enableAutoCleanup = !ItemCleaner.config.enableAutoCleanup;
        CleanupConfig.saveConfig(ItemCleaner.config);

        String messageKey = ItemCleaner.config.enableAutoCleanup
                ? "itemcleaner.toggle_auto_enabled"
                : "itemcleaner.toggle_auto_disabled";

        context.getSource().sendFeedback(
                () -> I18n.prefixedText(messageKey),
                true
        );
        return 1;
    }

    // 切换阈值检测
    private static int toggleThresholdCheck(CommandContext<ServerCommandSource> context) {
        ItemCleaner.config.enableThresholdCheck = !ItemCleaner.config.enableThresholdCheck;
        CleanupConfig.saveConfig(ItemCleaner.config);

        String messageKey = ItemCleaner.config.enableThresholdCheck
                ? "itemcleaner.toggle_threshold_enabled"
                : "itemcleaner.toggle_threshold_disabled";

        context.getSource().sendFeedback(
                () -> I18n.prefixedText(messageKey),
                true
        );
        return 1;
    }

    // 切换清理列表
    private static int switchCleanupList(CommandContext<ServerCommandSource> context) {
        String listName = StringArgumentType.getString(context, "listName");

        if (ItemCleaner.config.switchCleanupList(listName)) {
            CleanupConfig.saveConfig(ItemCleaner.config);
            context.getSource().sendFeedback(
                    () -> I18n.prefixedText("itemcleaner.list_switched", listName),
                    false
            );
        } else {
            context.getSource().sendFeedback(
                    () -> I18n.prefixedText("itemcleaner.list_not_found", listName),
                    false
            );
        }

        return 1;
    }

    // 创建清理列表（完善实现）
    private static int createCleanupList(CommandContext<ServerCommandSource> context) {
        String listName = StringArgumentType.getString(context, "listName");

        // 验证列表名合法性
        if (listName.contains(" ") || listName.isEmpty()) {
            context.getSource().sendFeedback(
                    () -> I18n.prefixedText("itemcleaner.invalid_list_name"),
                    false
            );
            return 0;
        }

        if (ItemCleaner.config.createCleanupList(listName)) {
            CleanupConfig.saveConfig(ItemCleaner.config);
            context.getSource().sendFeedback(
                    () -> I18n.prefixedText("itemcleaner.list_created", listName),
                    false
            );
            // 提示用户可以切换到新列表
            context.getSource().sendFeedback(
                    () -> Text.literal("§7使用 /cleandrops listmanager switch " + listName + " 切换到该列表§r"),
                    false
            );
        } else {
            context.getSource().sendFeedback(
                    () -> I18n.prefixedText("itemcleaner.list_already_exists", listName),
                    false
            );
        }

        return 1;
    }

    // 删除清理列表（完善实现）
    private static int deleteCleanupList(CommandContext<ServerCommandSource> context) {
        String listName = StringArgumentType.getString(context, "listName");

        // 防止删除最后一个列表
        if (ItemCleaner.config.cleanupLists.size() <= 1) {
            context.getSource().sendFeedback(
                    () -> I18n.prefixedText("itemcleaner.cannot_delete_last_list"),
                    false
            );
            return 0;
        }

        if (ItemCleaner.config.deleteCleanupList(listName)) {
            CleanupConfig.saveConfig(ItemCleaner.config);
            context.getSource().sendFeedback(
                    () -> I18n.prefixedText("itemcleaner.list_deleted", listName),
                    false
            );
        } else {
            if (ItemCleaner.config.currentCleanupList.equals(listName)) {
                context.getSource().sendFeedback(
                        () -> I18n.prefixedText("itemcleaner.list_cannot_delete_current", listName),
                        false
                );
                // 提示用户先切换列表
                context.getSource().sendFeedback(
                        () -> Text.literal("§7请先切换到其他列表再删除当前列表§r"),
                        false
                );
            } else {
                context.getSource().sendFeedback(
                        () -> I18n.prefixedText("itemcleaner.list_not_found", listName),
                        false
                );
            }
        }

        return 1;
    }

    // 显示当前使用的列表
    private static int showCurrentList(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(
                () -> I18n.prefixedText("itemcleaner.current_list", ItemCleaner.config.currentCleanupList),
                false
        );
        return 1;
    }

    // 列出所有列表
    private static int listAllLists(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(
                () -> Text.translatable("itemcleaner.all_lists_title"),
                false
        );

        Map<String, List<String>> allLists = ItemCleaner.config.cleanupLists;
        if (allLists.isEmpty()) {
            context.getSource().sendFeedback(
                    () -> Text.translatable("itemcleaner.no_lists_found"),
                    false
            );
            return 1;
        }

        for (String listName : allLists.keySet()) {
            String prefix = (ItemCleaner.config.currentCleanupList.equals(listName)) ? "§a* " : "§7- ";
            context.getSource().sendFeedback(
                    () -> Text.literal(prefix + listName + " §7(" + allLists.get(listName).size() + " 个物品)"),
                    false
            );
        }

        context.getSource().sendFeedback(
                () -> Text.literal("§7* 表示当前使用的列表§r"),
                false
        );

        return 1;
    }
}