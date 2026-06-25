package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Sable banks the camera with the sub-level orientation, which fights SSR's decoupled flat-world aim and
// makes the mouse move the view in strange ways on a tilted contraption. Skip the bank while shoulder
// surfing so the decoupled camera behaves; Sable already does this for its own unlocked view mode.
@Pseudo
@Mixin(targets = "dev.ryanhcode.sable.mixinhelpers.camera.camera_rotation.EntitySubLevelRotationHelper", remap = false)
public abstract class MixinSableSkipBankWhileShoulderSurfing {

    @Inject(method = "shouldCameraRotate", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void ssrcamerafixes$skipBankWhileShoulderSurfing(CallbackInfoReturnable<Boolean> cir) {
        if (ShoulderSurfingHelper.isShoulderSurfingActive()) {
            cir.setReturnValue(false);
        }
    }
}
