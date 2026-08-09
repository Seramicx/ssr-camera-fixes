package com.ssrcamerafixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TaCZ registers an adaptive-item callback that flags any held gun as "aiming", which makes SSR pin the
// body to the crosshair every tick and kills decoupled movement. Skipping that registration keeps the
// camera decoupled while holding a gun; shots still align via fire-time / tick-end crosshair snap.
@Pseudo
@Mixin(targets = "com.tacz.guns.compat.shouldersurfing.ShoulderSurfingPlugin", remap = false)
public abstract class MixinTaczGunHoldDecouple {

    @Inject(method = "register", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private void ssrcamerafixes$skipGunAdaptiveCallback(CallbackInfo ci) {
        ci.cancel();
    }
}
