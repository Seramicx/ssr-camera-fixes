package com.ssrcamerafixes.fabric.mixin;

import com.ssrcamerafixes.fabric.handler.ShoulderCycleHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// SSR consumes SWAP_SHOULDER in InputHandler.tick() during Minecraft.tick HEAD, before Fabric
// END_CLIENT_TICK, so drain the click here and let the cycle handler own the rising edge
@Pseudo
@Mixin(targets = "com.github.exopandora.shouldersurfing.client.InputHandler", remap = false)
public abstract class MixinSsrSuppressSwapOnCycleConflict {

    @Inject(method = "tick", at = @At("HEAD"), require = 0, remap = false)
    private void ssrcamerafixes$drainSwapIfCycleConflict(CallbackInfo ci) {
        ShoulderCycleHandler.drainSsrSwapIfConflicting();
    }
}
