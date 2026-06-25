package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.SableHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// SSR converts every setCameraType call into its own Perspective enum, which has no case for Sable's
// SUB_LEVEL_VIEW types, so cycling F5 onto a contraption camera is lost. Run before SSR's interceptor and
// set Sable's camera types straight onto the field so its F5 modes stay reachable alongside SSR's.
@Mixin(value = Options.class, priority = 900)
public abstract class MixinOptionsSableCameraPassthrough {

    @Shadow
    private CameraType cameraType;

    @Inject(method = "setCameraType", at = @At("HEAD"), cancellable = true)
    private void ssrcamerafixes$passSableCameraType(CameraType cameraType, CallbackInfo ci) {
        if (SableHelper.isSableCameraType(cameraType) || SableHelper.isSableCameraType(this.cameraType)) {
            this.cameraType = cameraType;
            ci.cancel();
        }
    }
}
