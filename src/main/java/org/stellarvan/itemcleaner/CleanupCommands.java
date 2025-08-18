package org.stellarvan.itemcleaner;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.Item;
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
                .executes(CleanupCommands::executeCleanup)

                .then(CommandManager.literal("add")
                        .executes(CleanupCommands::addHeldItem))

                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("itemId", StringArgumentType.string())
                                .executes(CleanupCommands::removeItem)))

                .then(CommandManager.literal("setinterval")
                        .then(CommandManager.argument("ticks", IntegerArgumentType.integer(200))
                                .executes(CleanupCommands::setInterval)))

                .then(CommandManager.literal("list")
                        .executes(CleanupCommands::listItems)));
    }

    private static int listItems(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        int count = ItemCleaner.config.itemsToClean.size();

        source.sendFeedback(() -> Text.literal("§a清理列表中的物品 (" + count + "个):§r"), false);

        for (String itemId : ItemCleaner.config.itemsToClean) {
            Identifier identifier = Identifier.of(itemId);
            Item item = Registries.ITEM.get(identifier);
            String itemName = item.getDefaultStack().getName().getString();

            source.sendFeedback(() -> Text.literal(
                    "§b- " + itemName + " §7(" + itemId + ")§r"
            ), false);
        }

        return 1;
    }

    private static int executeCleanup(CommandContext<ServerCommandSource> context) {
        ItemCleaner.cleanupTimer.performCleanup();
        context.getSource().sendFeedback(() -> Text.literal("§a已立即清理掉落物§r"), true);
        return 1;
    }

    private static int addHeldItem(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        if (player.getMainHandStack().isEmpty()) {
            context.getSource().sendFeedback(() -> Text.literal("§c你手中没有物品§r"), false);
            return 0;
        }

        String itemId = Registries.ITEM.getId(player.getMainHandStack().getItem()).toString();
        if (!ItemCleaner.config.itemsToClean.contains(itemId)) {
            ItemCleaner.config.itemsToClean.add(itemId);
            ItemCleaner.saveConfig();
            String itemName = player.getMainHandStack().getName().getString();
            context.getSource().sendFeedback(() -> Text.literal(
                    "§a已将 " + itemName + " §7(" + itemId + ")§a 添加到清理列表§r"
            ), true);
        } else {
            context.getSource().sendFeedback(() -> Text.literal(
                    "§e" + itemId + " 已在清理列表中§r"
            ), false);
        }
        return 1;
    }

    private static int removeItem(CommandContext<ServerCommandSource> context) {
        String itemId = StringArgumentType.getString(context, "itemId");
        String itemName;
        Identifier identifier = Identifier.of(itemId);
        if (Registries.ITEM.containsId(identifier)) {
            itemName = Registries.ITEM.get(identifier).getDefaultStack().getName().getString();
        } else {
            itemName = "未知物品";
        }

        if (ItemCleaner.config.itemsToClean.remove(itemId)) {
            ItemCleaner.saveConfig();
            context.getSource().sendFeedback(() -> Text.literal(
                    "§a已将 " + itemName + " §7(" + itemId + ")§a 从清理列表移除§r"
            ), true);
        } else {
            context.getSource().sendFeedback(() -> Text.literal(
                    "§c" + itemId + " 不在清理列表中§r"
            ), false);
        }
        return 1;
    }

    private static int setInterval(CommandContext<ServerCommandSource> context) {
        int ticks = IntegerArgumentType.getInteger(context, "ticks");
        ItemCleaner.config.cleanupInterval = ticks;
        ItemCleaner.saveConfig();

        ItemCleaner.cleanupTimer.tickCounter = 0;
        ItemCleaner.cleanupTimer.cleanupAnnounced = false;

        context.getSource().sendFeedback(() -> Text.literal(
                "§a已设置清理间隔为 " + ticks + " ticks（约 " + ticks/20 + " 秒）§r"
        ), true);
        return 1;
    }
}
