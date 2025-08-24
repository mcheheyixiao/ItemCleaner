package org.stellarvan.itemcleaner;

import net.minecraft.server.MinecraftServer;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

public class CleanupTimer {
    public MinecraftServer server;
    public int tickCounter = 0;
    public boolean cleanupAnnounced = false;
    private int thresholdCheckCounter = 0;
    private int warningCooldown = 0;

    public CleanupTimer() {
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
        ItemCleaner.LOGGER.info("服务器实例已设置");
        if (server != null) {
            int cooldownSeconds = ItemCleaner.config.warningCooldown / 20;
            ItemCleaner.LOGGER.info("当前清理配置 - 提示冷却时间: " + cooldownSeconds + "秒 (" +
                    ItemCleaner.config.warningCooldown + "ticks)");
        }
    }

    public void tick() {
        if (server == null) return;

        // 检查功能开关
        if (!ItemCleaner.config.enableAutoCleanup && !ItemCleaner.config.enableThresholdCheck) {
            return;
        }

        tickCounter++;
        thresholdCheckCounter++;
        int interval = ItemCleaner.config.cleanupInterval;
        int checkInterval = ItemCleaner.config.thresholdCheckInterval;

        // 冷却计时器递减
        if (warningCooldown > 0) {
            warningCooldown--;
        }

        // 数量检测（如果启用）
        if (ItemCleaner.config.enableThresholdCheck && thresholdCheckCounter >= checkInterval) {
            checkItemThreshold();
            thresholdCheckCounter = 0;
        }

        // 定时清理（如果启用）
        if (ItemCleaner.config.enableAutoCleanup && tickCounter >= interval) {
            ItemCleaner.LOGGER.info("达到清理间隔，执行定时清理");
            performCleanup(true);
            tickCounter = 0;
            cleanupAnnounced = false;
            return;
        }

        // 定时清理提示（使用lang文件文本）
        if (ItemCleaner.config.enableAutoCleanup && !cleanupAnnounced) {
            int remainingTicks = interval - tickCounter;
            if (remainingTicks == 1200) { // 1分钟
                sendCleanupWarning(I18n.text("itemcleaner.warning_1min"));
            } else if (remainingTicks == 600) { // 30秒
                sendCleanupWarning(I18n.text("itemcleaner.warning_30s"));
            } else if (remainingTicks == 100) { // 5秒
                sendCleanupWarning(I18n.text("itemcleaner.warning_5s"));
                cleanupAnnounced = true;
            }
        }
    }

    private void checkItemThreshold() {
        if (server == null) return;

        int cleanRadius = ItemCleaner.config.cleanRadius;
        int yMin = ItemCleaner.config.yMin;
        int yMax = ItemCleaner.config.yMax;
        int threshold = ItemCleaner.config.itemThreshold;
        int cooldownTicks = ItemCleaner.config.warningCooldown;

        if (cleanRadius <= 0) cleanRadius = 48;
        if (yMin >= yMax) {
            yMin = -64;
            yMax = 320;
        }

        final int finalRadius = cleanRadius;
        final int finalYMin = yMin;
        final int finalYMax = yMax;

        server.execute(() -> {
            AtomicInteger totalCount = new AtomicInteger(0);
            Set<ItemEntity> countedEntities = new HashSet<>();

            server.getWorlds().forEach(world -> {
                for (PlayerEntity player : world.getPlayers()) {
                    Vec3d playerPos = player.getPos();
                    Box playerRange = new Box(
                            playerPos.x - finalRadius, finalYMin, playerPos.z - finalRadius,
                            playerPos.x + finalRadius, finalYMax, playerPos.z + finalRadius
                    );

                    for (ItemEntity item : world.getEntitiesByClass(
                            ItemEntity.class, playerRange, entity -> true)) {
                        if (countedEntities.contains(item)) continue;

                        String itemId = Registries.ITEM.getId(item.getStack().getItem()).toString();
                        if (ItemCleaner.config.itemsToClean.contains(itemId)) {
                            totalCount.addAndGet(item.getStack().getCount());
                            countedEntities.add(item);
                        }
                    }
                }
            });

            ItemCleaner.LOGGER.debug("当前指定掉落物数量: " + totalCount.get() +
                    " (阈值: " + threshold + "), 冷却状态: " +
                    (warningCooldown > 0 ? "剩余" + warningCooldown/20 + "秒" : "就绪"));

            if (totalCount.get() >= threshold && warningCooldown <= 0) {
                ItemCleaner.LOGGER.info("数量达标且冷却结束，发送清理提示");
                InteractiveCleanupHandler.sendCleanupPrompt(server, totalCount.get());
                warningCooldown = cooldownTicks;
            }
        });
    }

    public void performCleanup(boolean forceNotify) {
        if (server == null) {
            ItemCleaner.LOGGER.error("执行清理失败：服务器实例为空");
            return;
        }

        int cleanRadius = ItemCleaner.config.cleanRadius;
        int yMin = ItemCleaner.config.yMin;
        int yMax = ItemCleaner.config.yMax;

        if (cleanRadius <= 0) cleanRadius = 48;
        if (yMin >= yMax) {
            yMin = -64;
            yMax = 320;
        }

        final int finalRadius = cleanRadius;
        final int finalYMin = yMin;
        final int finalYMax = yMax;

        server.execute(() -> {
            Map<String, Object[]> cleanedItems = new HashMap<>();
            AtomicInteger totalCleaned = new AtomicInteger(0);
            Set<ItemEntity> cleanedEntities = new HashSet<>();

            boolean doEntityDrops = server.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS);
            if (!doEntityDrops) {
                ItemCleaner.LOGGER.info("由于游戏规则设置，跳过清理");
                if (forceNotify) {
                    server.getPlayerManager().broadcast(
                            I18n.prefixedText("itemcleaner.cleanup_none"),
                            false
                    );
                }
                return;
            }

            server.getWorlds().forEach(world -> {
                for (PlayerEntity player : world.getPlayers()) {
                    Vec3d playerPos = player.getPos();
                    Box playerRange = new Box(
                            playerPos.x - finalRadius, finalYMin, playerPos.z - finalRadius,
                            playerPos.x + finalRadius, finalYMax, playerPos.z + finalRadius
                    );

                    for (ItemEntity item : world.getEntitiesByClass(
                            ItemEntity.class, playerRange, entity -> true)) {
                        if (cleanedEntities.contains(item)) continue;

                        String itemId = Registries.ITEM.getId(item.getStack().getItem()).toString();
                        if (ItemCleaner.config.itemsToClean.contains(itemId)) {
                            int count = item.getStack().getCount();
                            String itemName = item.getStack().getName().getString();

                            totalCleaned.addAndGet(count);
                            cleanedEntities.add(item);

                            if (cleanedItems.containsKey(itemId)) {
                                Object[] data = cleanedItems.get(itemId);
                                cleanedItems.put(itemId, new Object[]{data[0], (int)data[1] + count});
                            } else {
                                cleanedItems.put(itemId, new Object[]{itemName, count});
                            }

                            item.discard();
                        }
                    }
                }
            });

            sendCleanupStats(cleanedItems, totalCleaned.get(), forceNotify);
            ItemCleaner.LOGGER.info("清理完成，共清理 " + totalCleaned.get() + " 个物品");
            warningCooldown = 0;
        });
    }

    private void sendCleanupWarning(Text message) {
        server.getPlayerManager().broadcast(
                Text.translatable("itemcleaner.prefix").append(message),
                false
        );
    }

    private void sendCleanupStats(Map<String, Object[]> cleanedItems, int total, boolean forceNotify) {
        if (server == null) return;

        if (total > 0) {
            server.getPlayerManager().broadcast(
                    I18n.prefixedText("itemcleaner.cleanup_completed", total),
                    false
            );

            for (Map.Entry<String, Object[]> entry : cleanedItems.entrySet()) {
                String itemId = entry.getKey();
                String itemName = (String) entry.getValue()[0];
                int count = (int) entry.getValue()[1];

                server.getPlayerManager().broadcast(
                        Text.literal("§b- " + itemName + " §7(" + itemId + ")§a: " + count + " 个§r"),
                        false
                );
            }
        } else if (forceNotify) {
            server.getPlayerManager().broadcast(
                    I18n.prefixedText("itemcleaner.cleanup_none"),
                    false
            );
        }
    }
}
