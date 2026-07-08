package com.ssrcamerafixes.mixin;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera;
import com.ssrcamerafixes.SsrCameraFixesConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// v5 renderTick still computes renderRotation after the follow block, so cancelling at HEAD (the v4 approach)
// would freeze the camera. Redirect the follow guard instead, leaving renderRotation intact.
@Pseudo
@Mixin(value = ShoulderSurfingCamera.class, remap = false)
public abstract class MixinDisableFollowPlayerRotations {

    @Shadow
    protected abstract boolean isCameraTurningWithPlayer();

    @Redirect(
        method = "renderTick",
        at = @At(
            value = "INVOKE",
            target = "Lcom/github/exopandora/shouldersurfing/client/ShoulderSurfingCamera;isCameraTurningWithPlayer()Z"
        ),
        require = 0,
        remap = false
    )
    private boolean ssrcamerafixes$suppressFollow(ShoulderSurfingCamera camera) {
        boolean turning = this.isCameraTurningWithPlayer();
        try {
            return turning && !SsrCameraFixesConfig.DISABLE_FOLLOW_PLAYER_ROTATIONS.get();
        } catch (Throwable t) {
            return turning;
        }
    }
}
