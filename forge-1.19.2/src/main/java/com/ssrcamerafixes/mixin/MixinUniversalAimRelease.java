package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.handler.UniversalAimHandler;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class MixinUniversalAimRelease {

    // the release packet carries no rotation, so the server shoots along whatever was sent last tick
    @Inject(method = "releaseUsingItem", at = @At("HEAD"))
    private void ssrcamerafixes$aimBeforeRelease(Player player, CallbackInfo ci) {
        UniversalAimHandler.INSTANCE.beforeRelease();
    }
}
