package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.compat.SuperbWarfareHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// handlePlayerCamera (3P): setYaw(yaw + cameraRot - atan(|cameraLocation|/(lookDistance+2.9))*zoomPos).
// That atan is camera-only compensation for SW's lateral Camera.move — player lookAngle unchanged.
// Default cameraLocation is 0.6, so ADS always applies a yaw bias under vanilla 3P. Under SSR it
// desyncs the reticle from bullets. Skip the whole handler while SSR is active and pin location to 0
// so nothing else re-reads the stale 0.6 bias.
@Pseudo
@Mixin(targets = "com.atsuishio.superbwarfare.event.ClientEventHandler", remap = false)
public abstract class MixinSwSkipThirdPersonCameraYawOnSsr {

    @Inject(method = "handlePlayerCamera", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void ssrcamerafixes$skipSwCameraYawUnderSsr(CallbackInfo ci) {
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        SuperbWarfareHelper.clearCameraLocationBias();
        ci.cancel();
    }
}
