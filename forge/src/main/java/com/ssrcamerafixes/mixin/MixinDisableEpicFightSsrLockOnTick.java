package com.ssrcamerafixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


// Cancels EpicFight's ShoulderSurfingCompat.lockOnTick so BLO's lock-on
// camera lerp isn't overwritten with EF's movement-direction yaw.
@Pseudo
@Mixin(targets = "yesman.epicfight.compat.ShoulderSurfingCompat", remap = false)
public abstract class MixinDisableEpicFightSsrLockOnTick {

    @Inject(method = "lockOnTick", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void ssrcamerafixes$cancelEpicFightSsrLockOnTick(yesman.epicfight.api.client.event.types.LockOnEvent.Tick event, CallbackInfo ci) {
        ci.cancel();
    }
}
