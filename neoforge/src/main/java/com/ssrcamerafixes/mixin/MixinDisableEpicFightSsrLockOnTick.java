package com.ssrcamerafixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.LockOnSsrAim;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;

// Cancelling EF's SSR-aware lockOnTick made eye-based and camera-aware aim fight each other, so
// only record the facing it applies, for the unlock freeze
@Pseudo
@Mixin(targets = "yesman.epicfight.compat.shouldersurfing.ShoulderSurfingCompat", remap = false)
public abstract class MixinDisableEpicFightSsrLockOnTick {

    @Inject(method = "lockOnTick", at = @At("TAIL"), require = 0, remap = false)
    private static void ssrcamerafixes$rememberLockOnFacing(
            yesman.epicfight.api.client.event.types.camera.LockOnEvent.Tick event, CallbackInfo ci) {
        if (ShoulderSurfingHelper.isShoulderSurfingActive()) {
            LockOnSsrAim.rememberFacing(
                    ShoulderSurfingHelper.getCameraXRot(),
                    ShoulderSurfingHelper.getCameraYaw());
            return;
        }
        LockOnSsrAim.rememberFacing(
                EpicFightHelper.getLockOnXRot(1.0f),
                EpicFightHelper.getLockOnYRot(1.0f));
    }
}
