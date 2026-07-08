package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.FocusHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Focus cancels the vanilla crosshair overlay in any non-first-person view, but SSR's adaptive crosshair
// renders through that overlay, so Focus hides it during shoulder surfing. Keep the crosshair while shoulder
// surfing, except when Focus is locked on, where the locked target replaces the crosshair.
@Pseudo
@Mixin(targets = "com.jvn.focus.client.hud.VanillaCrosshairSuppressor", remap = false)
public abstract class MixinFocusKeepCrosshairForSsr {

    @Inject(method = "onPreRenderGuiLayer", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void ssrcamerafixes$keepSsrCrosshair(CallbackInfo ci) {
        if (ShoulderSurfingHelper.isShoulderSurfingActive() && !FocusHelper.isLockedOn()) {
            ci.cancel();
        }
    }
}
