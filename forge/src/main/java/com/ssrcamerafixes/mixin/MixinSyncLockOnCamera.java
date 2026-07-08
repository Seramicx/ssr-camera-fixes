package com.ssrcamerafixes.mixin;

import com.github.exopandora.shouldersurfing.api.math.Vec2f;
import com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera;
import com.ssrcamerafixes.compat.EpicFightHelper;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// v5 dropped calcRotations; the render pitch now lives in renderRotation, set at the end of renderTick.
// LockOnCameraSyncHandler still drives the angle during lock-on; this only eases the pitch back on release.
@Pseudo
@Mixin(value = ShoulderSurfingCamera.class, remap = false)
public class MixinSyncLockOnCamera {

    @Shadow private Vec2f renderRotation;

    @Unique private static boolean wasLockedOn;
    @Unique private static float pitchO;
    @Unique private static int blendFrames;

    @Inject(method = "renderTick", at = @At("TAIL"), require = 0, remap = false)
    private void ssrcamerafixes$blendLockOnPitch(Entity cameraEntity, float partialTick, CallbackInfo ci) {
        boolean lockedOn = EpicFightHelper.isLockOnTargeting();

        if (lockedOn) {
            pitchO = this.renderRotation.x();
            wasLockedOn = true;
            blendFrames = 0;
            return;
        }

        if (wasLockedOn) {
            blendFrames = 5;
            wasLockedOn = false;
        }

        if (blendFrames > 0) {
            float t = 1.0f - (float) blendFrames / 5.0f;
            float blended = pitchO + (this.renderRotation.x() - pitchO) * t;
            this.renderRotation = new Vec2f(blended, this.renderRotation.y());
            blendFrames--;
        }
    }
}
