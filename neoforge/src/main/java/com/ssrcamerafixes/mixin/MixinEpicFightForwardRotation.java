package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "yesman.epicfight.api.client.camera.EpicFightCameraAPI", remap = false)
public abstract class MixinEpicFightForwardRotation {

    // Mover skills (Phantom Ascent, Demolition Leap) launch along getForwardYRot/XRot, which falls back to the
    // body rotation while SSR has Epic Fight's TPS camera disabled, so they go forward instead of at the crosshair
    @Inject(method = "getForwardYRot", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void ssrcamerafixes$forwardYRotToCamera(CallbackInfoReturnable<Float> cir) {
        if (ShoulderSurfingHelper.isCameraDecoupled()) {
            cir.setReturnValue(ShoulderSurfingHelper.getCameraYaw());
        }
    }

    @Inject(method = "getForwardXRot", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void ssrcamerafixes$forwardXRotToCamera(CallbackInfoReturnable<Float> cir) {
        if (ShoulderSurfingHelper.isCameraDecoupled()) {
            cir.setReturnValue(ShoulderSurfingHelper.getCameraXRot());
        }
    }
}
