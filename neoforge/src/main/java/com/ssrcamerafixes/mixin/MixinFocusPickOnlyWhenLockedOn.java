package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Focus rewrites the pick every frame and fights SSR's, splitting the outline from the crosshair; while
// surfing, scope its correction to lock-on so SSR owns the normal-play pick.
@Pseudo
@Mixin(targets = "com.jvn.focus.client.FocusClientConfig", remap = false)
public abstract class MixinFocusPickOnlyWhenLockedOn {

    @Inject(method = "correctCrosshairOnlyWhileLockedOn", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void ssrcamerafixes$lockOnOnlyWhileSurfing(CallbackInfoReturnable<Boolean> cir) {
        if (ShoulderSurfingHelper.isShoulderSurfingActive()) {
            cir.setReturnValue(true);
        }
    }
}
