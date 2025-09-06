package org.stellarvan.itemcleaner;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.text.Text;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.util.Hand;
import net.minecraft.item.ItemStack;

public class CleanupToolItem extends HoeItem {

    public CleanupToolItem () {
        super (ToolMaterials.WOOD,1,-1.0f,new Settings());
        ItemGroupInitialize();
    }

    public static void ItemGroupInitialize() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register(entries -> {
                    entries.add(ItemCleaner.CLEANUP_HOE);
                });

    }

    @Override
    public TypedActionResult<ItemStack> use (World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand (hand);

        if (!world.isClient) {
            if (user.hasPermissionLevel (2)) {
                ItemCleaner.cleanupTimer.performCleanup (true);
                user.sendMessage (Text.translatable("itemcleaner.cleanup_start"), true);
                return TypedActionResult.success (stack);
            } else {
                user.sendMessage (Text.translatable("itemcleaner.no_permission"), true);
                return TypedActionResult.fail (stack);
            }
        }

        return TypedActionResult.pass (stack);
    }
}