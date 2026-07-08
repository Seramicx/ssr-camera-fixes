package com.ssrcamerafixes.fabric.mixin;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera;
import com.ssrcamerafixes.SsrCameraFixesConfig;
import com.ssrcamerafixes.SsrCameraFixesConfig.IdleBehavior;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// v5 moved the idle yaw-follow out of turn() into private turnPlayerWithCamera. Same job as the v4
// MixinSuppressIdleYawFollow, retargeted to the new method (setYRot is ordinal 0 there).
@Pseudo
@Mixin(value = ShoulderSurfingCamera.class, remap = false)
public abstract class MixinSuppressIdleYawFollowV5 {

    @Redirect(
        method = "turnPlayerWithCamera",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/util/Mth;approachDegrees(FFF)F",
            remap = true
        ),
        require = 0,
        remap = false
    )
    private float ssrcamerafixes$idleYawTarget(float lastMovedYRot, float target, float maxFollowAngle) {
        if (mode() == IdleBehavior.VANILLA_3RD_PERSON) {
            return target;
        }
        return Mth.approachDegrees(lastMovedYRot, target, maxFollowAngle);
    }

    @Redirect(
        method = "turnPlayerWithCamera",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;setYRot(F)V",
            ordinal = 0,
            remap = true
        ),
        require = 0,
        remap = false
    )
    private void ssrcamerafixes$idleSetYRot(LocalPlayer player, float playerYRot) {
        if (mode() == IdleBehavior.DECOUPLED) {
            return;
        }
        player.setYRot(playerYRot);
    }

    private static IdleBehavior mode() {
        try {
            return SsrCameraFixesConfig.IDLE_BEHAVIOR.get();
        } catch (Throwable t) {
            return IdleBehavior.DECOUPLED;
        }
    }
}
