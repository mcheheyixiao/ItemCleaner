package org.stellarvan.itemcleaner.mixin;

import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.stellarvan.itemcleaner.ItemCleaner;

@Mixin(MinecraftServer.class)
public abstract class ServerMixin {
    @Inject(at = @At("HEAD"), method = "tick")
    private void tick(CallbackInfo info) {
        MinecraftServer server = (MinecraftServer)(Object)this;

        if (ItemCleaner.cleanupTimer != null && ItemCleaner.cleanupTimer.server == null) {
            ItemCleaner.setServer(server);
        }

        if (ItemCleaner.cleanupTimer != null) {
            ItemCleaner.cleanupTimer.tick();
        }
    }
}
