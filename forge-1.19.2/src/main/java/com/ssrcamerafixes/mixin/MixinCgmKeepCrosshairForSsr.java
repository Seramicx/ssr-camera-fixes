package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// CGM cancels CROSSHAIR once ADS progress > 0.5 and returns without drawing anything in third person.
// SSR's adaptive crosshair uses that overlay, so RMB-aim hides it. Skip CGM's blanking path under SSR.
@Pseudo
@Mixin(targets = "com.mrcrayfish.guns.client.handler.CrosshairHandler", remap = false)
public abstract class MixinCgmKeepCrosshairForSsr {

    @Inject(method = "onRenderOverlay", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void ssrcamerafixes$keepSsrCrosshair(RenderGuiOverlayEvent.Pre event, CallbackInfo ci) {
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!VanillaGuiOverlay.CROSSHAIR.type().equals(event.getOverlay())) return;
        try {
            Object aiming = Class.forName("com.mrcrayfish.guns.client.handler.AimingHandler")
                    .getMethod("get").invoke(null);
            Object progress = aiming.getClass().getMethod("getNormalisedAdsProgress").invoke(aiming);
            if (progress instanceof Double && (Double) progress > 0.5D) {
                ci.cancel();
            }
        } catch (Throwable ignored) {}
    }
}
