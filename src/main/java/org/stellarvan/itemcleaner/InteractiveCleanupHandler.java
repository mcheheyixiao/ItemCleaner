package org.stellarvan.itemcleaner;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

// 处理交互式清理确认
public class InteractiveCleanupHandler {
    private static int requestId = 0;

    // 生成新的请求ID
    public static synchronized int generateRequestId() {
        return requestId++;
    }

    // 发送带有Yes/No按钮的提示
    public static void sendCleanupPrompt(MinecraftServer server, int itemCount) {
        if (server == null) return;

        int requestId = generateRequestId();

        // 创建带有可点击组件的消息，使用MutableText确保append方法可用
        MutableText message = Text.literal(I18n.translate("itemcleaner.prefix") +
                I18n.translate("itemcleaner.threshold_prompt", itemCount)
        ).append(
                // Yes按钮 - 使用cleandrops命令
                Text.literal(I18n.translate("itemcleaner.button_yes")).formatted(Formatting.GREEN)
                        .styled(style -> style.withClickEvent(
                                new net.minecraft.text.ClickEvent(
                                        net.minecraft.text.ClickEvent.Action.RUN_COMMAND,
                                        "/cleandrops confirm " + requestId + " yes"
                                )
                        ).withHoverEvent(
                                new net.minecraft.text.HoverEvent(
                                        net.minecraft.text.HoverEvent.Action.SHOW_TEXT,
                                        Text.literal("点击立即清理")
                                )
                        ))
        ).append(
                Text.literal("  ").formatted(Formatting.WHITE)
        ).append(
                // No按钮 - 使用cleandrops命令
                Text.literal(I18n.translate("itemcleaner.button_no")).formatted(Formatting.RED)
                        .styled(style -> style.withClickEvent(
                                new net.minecraft.text.ClickEvent(
                                        net.minecraft.text.ClickEvent.Action.RUN_COMMAND,
                                        "/cleandrops confirm " + requestId + " no"
                                )
                        ).withHoverEvent(
                                new net.minecraft.text.HoverEvent(
                                        net.minecraft.text.HoverEvent.Action.SHOW_TEXT,
                                        Text.literal("不清理，等待定时清理")
                                )
                        ))
        );

        server.getPlayerManager().broadcast(message, false);
        ItemCleaner.LOGGER.info("发送掉落物上限提示，请求ID: " + requestId);
    }

    // 处理确认选择
    public static int handleConfirmation(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        String requestIdStr = context.getArgument("requestId", String.class);
        int requestId;
        try {
            requestId = Integer.parseInt(requestIdStr);
        } catch (NumberFormatException e) {
            context.getSource().sendError(Text.literal("无效的请求ID"));
            return 0;
        }

        String choice = context.getArgument("choice", String.class);

        if ("yes".equalsIgnoreCase(choice)) {
            // 执行立即清理
            ItemCleaner.cleanupTimer.performCleanup(true);
            context.getSource().sendFeedback(
                    () -> I18n.prefixedText("itemcleaner.choice_yes_feedback"),
                    true
            );
            ItemCleaner.LOGGER.info("玩家 " + player.getName().getString() + " 确认立即清理，请求ID: " + requestId);
        } else {
            // 不清理，等待定时清理
            context.getSource().sendFeedback(
                    () -> I18n.prefixedText("itemcleaner.choice_no_feedback"),
                    false
            );
            ItemCleaner.LOGGER.info("玩家 " + player.getName().getString() + " 选择不立即清理，请求ID: " + requestId);
        }

        return 1;
    }
}
