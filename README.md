# ItemCleaner - 我的世界掉落物清理模组

![ItemCleaner Logo](src/main/resources/assets/itemcleaner/icon.png)

ItemCleaner 是一个基于 Fabric 开发的 Minecraft 模组，旨在提供灵活、可配置的掉落物清理功能。该模组支持定时自动清理、自定义清理范围、物品白名单管理，并通过直观的聊天提示反馈清理结果，适用于服务器和单机游戏。


## 功能特点

### 核心清理功能
- ✅**定时自动清理**：可通过配置文件设置清理间隔（默认20分钟），自动清理指定物品
- ✅**阈值实时检测**：当掉落物数量达到指定阈值时，会显示一个提示，并由玩家决定是否清理
- ✅**玩家中心范围清理**：以每个在线玩家为中心，清理其周围一定范围内的掉落物（范围可配置）
- ✅**手动清理命令**：支持通过指令立即执行清理，方便临时操作

### 灵活的配置系统
- ✅**精确配置**：可精确指定清理开关、清理间隔、清理范围、物品阈值、语言包等参数
- ✅**自定义清理半径**：可设置水平清理半径（默认48格，即3个区块）和垂直范围（默认-64至320）
- ✅**物品白名单管理**：精确指定需要清理的物品（支持通过物品ID配置）
- ✅**提示信息定制**：可自定义清理倒计时提示文本及颜色（支持Minecraft颜色代码`§`）
- ✅**多语言支持**：支持中文和英文，可自定义语言包

### 直观的用户反馈
- ✅️**聊天栏提示**：清理前的倒计时提醒（1分钟、30秒、5秒）
- ✅**详细清理统计**：清理完成后显示总数量及每个物品的名称、ID和数量
- ✅**本地化支持**：单人游戏也支持使用


## 安装方法
将下载的模组JAR文件放入 `mods` 文件夹中

### 前置依赖
- Minecraft 1.21.1
- Fabric Loader 0.17.2+
- Fabric API 0.116.5+

### 安装步骤
1. 下载对应版本的模组JAR文件
2. 将JAR文件放入 Minecraft 目录下的 `mods` 文件夹：
    - **客户端**：直接放入客户端 `mods` 文件夹，支持单人游戏
    - **服务端**：放入服务端 `mods` 文件夹，所有玩家共享清理规则
3. 启动游戏/服务器，模组会自动生成默认配置文件

##指令介绍
- **/cleandrops** ：显示模组的指令介绍
- **/cleandrops help** ：发送详细的帮助说明
- **/cleandrops clean** ：立即清理指定掉落物（依据清理列表）
- **/cleandrops add** ：将当前手持的物品添加到清理列表
- **/cleandrops remove <itemID>** ：从清理列表中移除指定 ID 的物品（需填写物品完整 ID）
- **/cleandrops list** ：显示清理列表中的所有物品
- **/cleandrops toggle auto** ：切换定时自动清理功能的开启 / 关闭
- **/cleandrops toggle threshold** ：切换掉落物数量阈值提示功能的开启 / 关闭
- **/cleandrops setinterval <ticks>** ：设置自动清理的间隔时间（1 秒 = 20 游戏刻）
- **/cleandrops language <langCode>** ：切换模组的显示语言（支持 zh_cn 中文 /en_us 英文）

## 配置说明

配置文件路径：`./config/itemcleaner.json`（首次启动后自动生成）

### 核心配置参数
```json
{
  "enableAutoCleanup": true,  // 是否开启定时自动清理功能
  "enableThresholdCheck": true,  // 是否开启掉落物数量阈值提示功能
  "cleanupInterval": 12000,  // 自动清理间隔（游戏刻）
  "thresholdCheckInterval": 100,  // 掉落物数量阈值提示间隔（游戏刻）
  "warningCooldown": 1200,  // 掉落物数量阈值提示冷却时间（游戏刻）
  "cleanRadius": 48,  // 清理半径（方块）
  "yMin": -64,  // 清理范围最小高度
  "yMax": 320,  // 清理范围最大高度
  "itemThreshold": 100,  // 掉落物数量阈值
  "language": "zh_cn",  // 模组的显示语言
  "itemsToClean": [  // 需要清理的物品列表
    "minecraft:cobblestone",
    "minecraft:dirt",
    "minecraft:jungle_wood"
  ]
}