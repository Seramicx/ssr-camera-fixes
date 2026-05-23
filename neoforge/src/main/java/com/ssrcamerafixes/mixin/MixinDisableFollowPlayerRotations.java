package com.ssrcamerafixes.mixin;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera;
import com.ssrcamerafixes.SsrCameraFixesConfig;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ShoulderSurfingCamera.class, remap = false)
public abstract class MixinDisableFollowPlayerRotations {

    @Inject(method = "renderTick", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void ssrcamerafixes$cancelFollowPlayerRotations(Entity cameraEntity, float partialTick, CallbackInfo ci) {
        try {
            if (SsrCameraFixesConfig.DISABLE_FOLLOW_PLAYER_ROTATIONS.get()) {
                ci.cancel();
            }
        } catch (Throwable ignored) {
        }
    }
}
