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

    public CleanupTimer() {
    }

    public void setServer(MinecraftServer server) {
        this.server = server;
        ItemCleaner.LOGGER.info("服务器实例已设置");
        if (server != null) {
            ItemCleaner.LOGGER.info("当前清理配置 - 半径: " + ItemCleaner.config.cleanRadius +
                    "格, Y范围: " + ItemCleaner.config.yMin + "~" + ItemCleaner.config.yMax);
        }
    }

    public void tick() {
        if (server == null) {
            ItemCleaner.LOGGER.debug("服务器实例为空，跳过tick");
            return;
        }

        tickCounter++;
        int interval = ItemCleaner.config.cleanupInterval;

        if (tickCounter >= interval) {
            ItemCleaner.LOGGER.info("达到清理间隔，执行定时清理");
            performCleanup(true);
            tickCounter = 0;
            cleanupAnnounced = false;
            return;
        }

        if (!cleanupAnnounced) {
            int remainingTicks = interval - tickCounter;

            if (remainingTicks == 1200 || remainingTicks == 600 || remainingTicks == 100) {
                sendCleanupWarning(remainingTicks);
                if (remainingTicks == 100) {
                    cleanupAnnounced = true;
                }
            }
        }
    }

    private void sendCleanupWarning(int remainingTicks) {
        String message;
        int seconds = remainingTicks / 20;

        if (seconds == 60) {
            message = ItemCleaner.config.warning1Minute;
        } else if (seconds == 30) {
            message = ItemCleaner.config.warning30Seconds;
        } else if (seconds == 5) {
            message = ItemCleaner.config.warning5Seconds;
        } else {
            return;
        }

        server.getPlayerManager().broadcast(Text.literal(message), false);
        ItemCleaner.LOGGER.info("发送清理提示: " + message.replaceAll("§.", ""));
    }

    public void performCleanup(boolean forceNotify) {
        if (server == null) {
            ItemCleaner.LOGGER.error("执行清理失败：服务器实例为空");
            return;
        }

        int cleanRadius = ItemCleaner.config.cleanRadius;
        int yMin = ItemCleaner.config.yMin;
        int yMax = ItemCleaner.config.yMax;

        if (cleanRadius <= 0) {
            ItemCleaner.LOGGER.warn("无效的清理半径配置 (" + cleanRadius + "), 使用默认值48格");
            cleanRadius = 48;
        }
        if (yMin >= yMax) {
            ItemCleaner.LOGGER.warn("无效的Y轴范围配置 (" + yMin + "~" + yMax + "), 使用默认范围");
            yMin = -64;
            yMax = 320;
        }

        // 关键修复：创建final副本，供lambda表达式使用
        final int finalRadius = cleanRadius;
        final int finalYMin = yMin;
        final int finalYMax = yMax;

        server.execute(() -> {
            // 检查游戏规则
            boolean doEntityDrops = server.getGameRules().getBoolean(GameRules.DO_ENTITY_DROPS);
            if (!doEntityDrops) {
                ItemCleaner.LOGGER.info("由于游戏规则设置，跳过清理");
                if (forceNotify) {
                    server.getPlayerManager().broadcast(
                            Text.literal("§a[服务器娘] 定时清理已执行，但由于游戏规则设置未清理任何物品§r"),
                            false
                    );
                }
                return;
            }

            Map<String, Object[]> cleanedItems = new HashMap<>();
            AtomicInteger totalCleaned = new AtomicInteger(0);
            Set<ItemEntity> cleanedEntities = new HashSet<>();

            server.getWorlds().forEach(world -> {
                for (PlayerEntity player : world.getPlayers()) {
                    Vec3d playerPos = player.getPos();
                    // 使用final副本变量，解决lambda引用问题
                    Box playerRange = new Box(
                            playerPos.x - finalRadius, finalYMin, playerPos.z - finalRadius,
                            playerPos.x + finalRadius, finalYMax, playerPos.z + finalRadius
                    );

                    ItemCleaner.LOGGER.debug(
                            "清理玩家 " + player.getName().getString() + " 周围区域: " +
                                    "半径=" + finalRadius + "格, " +
                                    "X: " + (int)(playerPos.x - finalRadius) + "~" + (int)(playerPos.x + finalRadius) +
                                    ", Y: " + finalYMin + "~" + finalYMax +
                                    ", Z: " + (int)(playerPos.z - finalRadius) + "~" + (int)(playerPos.z + finalRadius)
                    );

                    for (ItemEntity item : world.getEntitiesByClass(
                            ItemEntity.class,
                            playerRange,
                            entity -> true
                    )) {
                        if (cleanedEntities.contains(item)) continue;

                        String itemId = Registries.ITEM.getId(item.getStack().getItem()).toString();
                        int count = item.getStack().getCount();
                        String itemName = item.getStack().getName().getString();

                        if (ItemCleaner.config.itemsToClean.contains(itemId)) {
                            item.discard();
                            cleanedEntities.add(item);
                            totalCleaned.addAndGet(count);

                            if (cleanedItems.containsKey(itemId)) {
                                cleanedItems.compute(itemId, (k, data) -> new Object[]{data[0], (int) data[1] + count});
                            } else {
                                cleanedItems.put(itemId, new Object[]{itemName, count});
                            }
                        }
                    }
                }
            });

            sendCleanupStats(cleanedItems, totalCleaned.get(), forceNotify);
            ItemCleaner.LOGGER.info("清理完成，共清理 " + totalCleaned.get() + " 个物品");
        });
    }

    public void performCleanup() {
        performCleanup(false);
    }

    private void sendCleanupStats(Map<String, Object[]> cleanedItems, int total, boolean forceNotify) {
        if (server == null) return;

        if (total > 0) {
            server.getPlayerManager().broadcast(
                    Text.literal("§a[服务器娘] 本次清理结果: 共清理 " + total + " 个物品§r"),
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
                    Text.literal("§a[服务器娘] 定时清理已执行，未发现需要清理的掉落物§r"),
                    false
            );
        }
    }
}
