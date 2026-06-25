package com.ssrcamerafixes.mixin;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera;
import com.ssrcamerafixes.SsrCameraFixesConfig;
import com.ssrcamerafixes.SsrCameraFixesConfig.IdleBehavior;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// v4 ran the idle yaw-follow inside turn(); v5 moved it to turnPlayerWithCamera(). common compiles against both,
// so both targets are wired with require=0 and whichever exists at runtime applies.
@Pseudo
@Mixin(value = ShoulderSurfingCamera.class, remap = false)
public abstract class MixinSuppressIdleYawFollow {

    @Redirect(
        method = "turn",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;approachDegrees(FFF)F", remap = true),
        require = 0,
        remap = false
    )
    private float ssrcamerafixes$idleYawTargetV4(float lastMovedYRot, float target, float maxFollowAngle) {
        if (mode() == IdleBehavior.VANILLA_3RD_PERSON) {
            return target;
        }
        return Mth.approachDegrees(lastMovedYRot, target, maxFollowAngle);
    }

    @Redirect(
        method = "turn",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setYRot(F)V", ordinal = 1, remap = true),
        require = 0,
        remap = false
    )
    private void ssrcamerafixes$idleSetYRotV4(LocalPlayer player, float playerYRot) {
        if (mode() == IdleBehavior.DECOUPLED) {
            return;
        }
        player.setYRot(playerYRot);
    }

    @Redirect(
        method = "turnPlayerWithCamera",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;approachDegrees(FFF)F", remap = true),
        require = 0,
        remap = false
    )
    private float ssrcamerafixes$idleYawTargetV5(float lastMovedYRot, float target, float maxFollowAngle) {
        if (mode() == IdleBehavior.VANILLA_3RD_PERSON) {
            return target;
        }
        return Mth.approachDegrees(lastMovedYRot, target, maxFollowAngle);
    }

    @Redirect(
        method = "turnPlayerWithCamera",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setYRot(F)V", ordinal = 0, remap = true),
        require = 0,
        remap = false
    )
    private void ssrcamerafixes$idleSetYRotV5(LocalPlayer player, float playerYRot) {
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
