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

public class CleanupCommands {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher,
                                CommandRegistryAccess registryAccess,
                                CommandManager.RegistrationEnvironment environment) {
        dispatcher.register(CommandManager.literal("cleandrops")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    // 使用带前缀的文本
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
                        .executes(CleanupCommands::listItems))
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
                                .executes(CleanupCommands::setLanguage))));
    }

    // 语言切换命令
    private static int setLanguage(CommandContext<ServerCommandSource> context) {
        String langCode = StringArgumentType.getString(context, "langCode");
        ItemCleaner.config.language = langCode;
        ItemCleaner.saveConfig();
        I18n.setLanguage(langCode);

        // 确保使用带前缀的反馈
        context.getSource().sendFeedback(
                () -> I18n.prefixedText("itemcleaner.language_updated", langCode),
                false
        );
        return 1;
    }

    // 帮助命令
    private static int showHelp(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        source.sendFeedback(() -> I18n.text("itemcleaner.help_title"), false);
        source.sendFeedback(() -> I18n.text("itemcleaner.help_command_help"), false);
        source.sendFeedback(() -> I18n.text("itemcleaner.help_command_clean"), false);
        source.sendFeedback(() -> I18n.text("itemcleaner.help_command_add"), false);
        source.sendFeedback(() -> I18n.text("itemcleaner.help_command_remove"), false);
        source.sendFeedback(() -> I18n.text("itemcleaner.help_command_setinterval"), false);
        source.sendFeedback(() -> I18n.text("itemcleaner.help_command_list"), false);
        source.sendFeedback(() -> I18n.text("itemcleaner.help_command_toggle_auto"), false);
        source.sendFeedback(() -> I18n.text("itemcleaner.help_command_toggle_threshold"), false);
        source.sendFeedback(() -> I18n.text("itemcleaner.help_command_language"), false);
        source.sendFeedback(() -> I18n.text("itemcleaner.help_note"), false);

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

        if (!ItemCleaner.config.itemsToClean.contains(itemIdStr)) {
            ItemCleaner.config.itemsToClean.add(itemIdStr);
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

        if (ItemCleaner.config.itemsToClean.remove(itemId)) {
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

    // 列出物品
    private static int listItems(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(
                () -> I18n.text("itemcleaner.list_title"),
                false
        );

        if (ItemCleaner.config.itemsToClean.isEmpty()) {
            context.getSource().sendFeedback(
                    () -> I18n.text("itemcleaner.list_empty"),
                    false
            );
            return 1;
        }

        for (String itemId : ItemCleaner.config.itemsToClean) {
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
}
