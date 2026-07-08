package com.ssrcamerafixes.fabric1211.mixin;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera;
import com.ssrcamerafixes.compat.BetterMountSteeringHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = ShoulderSurfingCamera.class, remap = false)
public abstract class MixinSsrSuppressFollowDuringMountRotate {

    @Shadow private int followPlayerRotationsDelay;

    @Inject(method = "tick", at = @At("HEAD"), require = 0, remap = false)
    private void ssrcamerafixes$preventFollowDuringMountRotate(CallbackInfo ci) {
        boolean active = BetterMountSteeringHelper.isMountRotateActive()
                || BetterMountSteeringHelper.isDecoupleTransitioning();
        if (active && this.followPlayerRotationsDelay < 2) {
            this.followPlayerRotationsDelay = 2;
        }
    }
}
