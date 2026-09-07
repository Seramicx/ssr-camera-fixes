package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// JEG cancels the vanilla crosshair overlay whenever a gun is held, then draws its own only in first person or when
// its own Shoulder Surfing check passes. That check resolves ShoulderSurfingImpl, gone since SSR v5, so it always
// answers false and third person ends up with no crosshair at all
@Pseudo
@Mixin(targets = "ttv.migami.jeg.client.handler.CrosshairHandler", remap = false)
public abstract class MixinJegCrosshairInShoulder {

    @Redirect(method = "onRenderOverlay",
            at = @At(value = "INVOKE", target = "Lttv/migami/jeg/compat/ShoulderSurfingHelper;isShoulderSurfing()Z"),
            require = 0, remap = false)
    private boolean ssrcamerafixes$crosshairInShoulderView() {
        return ShoulderSurfingHelper.isShoulderSurfingActive();
    }
}
