package com.ssrcamerafixes.mixin;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera;
import com.ssrcamerafixes.SsrCameraFixesConfig;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancel SSR's {@code renderTick} when {@code disableFollowPlayerRotations} is on.
 *
 * <p>SSR's {@code ShoulderSurfingCamera.renderTick} is where the idle-camera
 * follow lives — when the user hasn't moved the mouse for
 * {@code followPlayerRotationsDelay} ticks, that method lerps {@code this.yRot}
 * toward {@code EntityHelper.getLerpedYRot(cameraEntity)} so the camera drifts
 * to face the player's body direction. Some users find that disorienting.
 *
 * <p>Cancelling at HEAD skips the entire body — there is no other work in
 * {@code renderTick} besides the follow lerp (see ShoulderSurfingCamera:128-137).
 */
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
