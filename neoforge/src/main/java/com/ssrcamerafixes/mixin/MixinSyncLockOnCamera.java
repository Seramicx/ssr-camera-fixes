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

// On NeoForge the lock-on angles must be read straight from EpicFight; ComputeCameraAngles is circular here.
// v5 has no calcRotations, so the override lands on renderTick's final renderRotation (Vec2f x=pitch, y=yaw).
@Pseudo
@Mixin(value = ShoulderSurfingCamera.class, remap = false)
public abstract class MixinSyncLockOnCamera {

    @Shadow private Vec2f renderRotation;

    @Unique private static boolean wasLockedOn;
    @Unique private static float pitchO;
    @Unique private static int blendFrames;

    @Inject(method = "renderTick", at = @At("TAIL"), require = 0, remap = false)
    private void ssrcamerafixes$syncLockOn(Entity cameraEntity, float partialTick, CallbackInfo ci) {
        boolean lockedOn = EpicFightHelper.isLockOnTargeting();

        if (lockedOn) {
            float pitch = EpicFightHelper.getLockOnXRot(partialTick);
            float yaw = EpicFightHelper.getLockOnYRot(partialTick);
            this.renderRotation = new Vec2f(pitch, yaw);
            pitchO = pitch;
            wasLockedOn = true;
            blendFrames = 0;
            return;
        }

        if (wasLockedOn) {
            blendFrames = 5;
            wasLockedOn = false;
        }

        if (blendFrames > 0) {
            float t = 1.0F - blendFrames / 5.0F;
            float pitch = pitchO + (this.renderRotation.x() - pitchO) * t;
            this.renderRotation = new Vec2f(pitch, this.renderRotation.y());
            blendFrames--;
        }
    }
}
