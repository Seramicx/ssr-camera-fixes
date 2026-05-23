package com.ssrcamerafixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "yesman.epicfight.compat.shouldersurfing.ShoulderSurfingCompat", remap = false)
public abstract class MixinDisableEpicFightSsrLockOnTick {

    @Inject(method = "lockOnTick", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void ssrcamerafixes$cancelEpicFightSsrLockOnTick(yesman.epicfight.api.client.event.types.camera.LockOnEvent.Tick event, CallbackInfo ci) {
        ci.cancel();
    }
}
