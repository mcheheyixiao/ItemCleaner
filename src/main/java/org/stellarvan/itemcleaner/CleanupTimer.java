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
            ItemCleaner.LOGGER.info("阈值检测配置 - 启用: " + ItemCleaner.config.enableThresholdCheck +
                    ", 检测间隔: " + ItemCleaner.config.thresholdCheckInterval + "ticks, " +
                    "触发阈值: " + ItemCleaner.config.itemThreshold);
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
        if (ItemCleaner.config.enableThresholdCheck) {
            if (checkInterval <= 0) {
                checkInterval = 200;
                ItemCleaner.LOGGER.warn("阈值检测间隔配置无效，使用默认值: " + checkInterval + "ticks");
            }

            if (thresholdCheckCounter % 100 == 0) {
                ItemCleaner.LOGGER.debug("阈值检测计数器 - 当前: " + thresholdCheckCounter +
                        ", 间隔: " + checkInterval);
            }

            if (thresholdCheckCounter >= checkInterval) {
                ItemCleaner.LOGGER.debug("达到阈值检测间隔，执行检测");
                checkItemThreshold();
                thresholdCheckCounter = 0;
            }
        }

        // 定时清理（如果启用）
        if (ItemCleaner.config.enableAutoCleanup && tickCounter >= interval) {
            ItemCleaner.LOGGER.info("达到清理间隔，执行定时清理");
            performCleanup(true);
            tickCounter = 0;
            cleanupAnnounced = false;
            return;
        }

        // 定时清理提示
        if (ItemCleaner.config.enableAutoCleanup && !cleanupAnnounced) {
            int remainingTicks = interval - tickCounter;
            if (remainingTicks == 1200) { // 1分钟
                sendCleanupWarning("itemcleaner.warning_1min");
            } else if (remainingTicks == 600) { // 30秒
                sendCleanupWarning("itemcleaner.warning_30s");
            } else if (remainingTicks == 100) { // 5秒
                sendCleanupWarning("itemcleaner.warning_5s");
                cleanupAnnounced = true;
            }
        }
    }

    private void checkItemThreshold() {
        if (server == null) {
            ItemCleaner.LOGGER.warn("跳过阈值检测：服务器实例为空");
            return;
        }

        int cleanRadius = ItemCleaner.config.cleanRadius;
        int yMin = ItemCleaner.config.yMin;
        int yMax = ItemCleaner.config.yMax;
        int threshold = ItemCleaner.config.itemThreshold;
        int cooldownTicks = ItemCleaner.config.warningCooldown;

        if (threshold <= 0) {
            threshold = 100;
            ItemCleaner.LOGGER.warn("阈值配置无效(" + threshold + ")，使用默认值: 100");
        }
        if (cleanRadius <= 0) {
            cleanRadius = 48;
            ItemCleaner.LOGGER.debug("清理半径无效，使用默认值: " + cleanRadius);
        }
        if (yMin >= yMax) {
            yMin = -64;
            yMax = 320;
            ItemCleaner.LOGGER.debug("Y轴范围无效，使用默认值: " + yMin + "~" + yMax);
        }
        if (cooldownTicks <= 0) {
            cooldownTicks = 1200;
            ItemCleaner.LOGGER.debug("警告冷却无效，使用默认值: " + cooldownTicks + "ticks");
        }

        final int finalRadius = cleanRadius;
        final int finalYMin = yMin;
        final int finalYMax = yMax;
        final int finalThreshold = threshold;
        final int finalCooldown = cooldownTicks;

        server.execute(() -> {
            AtomicInteger totalCount = new AtomicInteger(0);
            Set<ItemEntity> countedEntities = new HashSet<>();
            Set<String> detectedItems = new HashSet<>();

            server.getWorlds().forEach(world -> {
                if (world == null) {
                    ItemCleaner.LOGGER.debug("跳过空世界的物品检测");
                    return;
                }

                for (PlayerEntity player : world.getPlayers()) {
                    Vec3d playerPos = player.getPos();
                    Box playerRange = new Box(
                            playerPos.x - finalRadius, finalYMin, playerPos.z - finalRadius,
                            playerPos.x + finalRadius, finalYMax, playerPos.z + finalRadius
                    );

                    if (totalCount.get() == 0) {
                        ItemCleaner.LOGGER.debug("检测范围 - 玩家: " + player.getName().getString() +
                                ", 位置: (" + (int)playerPos.x + "," + (int)playerPos.y + "," + (int)playerPos.z + "), " +
                                "半径: " + finalRadius + ", Y范围: " + finalYMin + "~" + finalYMax);
                    }

                    // 修复：使用当前活跃列表替代原来的itemsToClean
                    for (ItemEntity item : world.getEntitiesByClass(
                            ItemEntity.class, playerRange, entity -> true)) {
                        if (countedEntities.contains(item)) continue;

                        String itemId = Registries.ITEM.getId(item.getStack().getItem()).toString();
                        // 关键修复：使用getActiveCleanupList()获取当前列表
                        if (ItemCleaner.config.getActiveCleanupList().contains(itemId)) {
                            int count = item.getStack().getCount();
                            totalCount.addAndGet(count);
                            countedEntities.add(item);
                            detectedItems.add(itemId);

                            ItemCleaner.LOGGER.debug("检测到清理列表物品: " + itemId + " x" + count);
                        }
                    }
                }
            });

            ItemCleaner.LOGGER.debug("阈值检测结果 - 总数量: " + totalCount.get() +
                    ", 阈值: " + finalThreshold +
                    ", 检测到的物品类型: " + detectedItems.size() +
                    ", 冷却状态: " + (warningCooldown > 0 ? "剩余" + warningCooldown/20 + "秒" : "就绪"));

            if (totalCount.get() >= finalThreshold && warningCooldown <= 0) {
                ItemCleaner.LOGGER.info("数量达标且冷却结束，发送清理提示 (数量: " + totalCount.get() + ")");
                InteractiveCleanupHandler.sendCleanupPrompt(server, totalCount.get());
                warningCooldown = finalCooldown;
            } else if (totalCount.get() >= finalThreshold) {
                ItemCleaner.LOGGER.debug("数量达标但冷却中，不发送提示");
            } else if (warningCooldown <= 0) {
                ItemCleaner.LOGGER.debug("未达阈值，不发送提示");
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
                        // 关键修复：使用当前活跃列表
                        if (ItemCleaner.config.getActiveCleanupList().contains(itemId)) {
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

    private void sendCleanupWarning(String messageKey) {
        server.getPlayerManager().broadcast(
                I18n.prefixedText(messageKey),
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
