package com.ssrcamerafixes.mixin;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera;
import com.ssrcamerafixes.compat.EpicFightHelper;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ShoulderSurfingCamera.class, remap = false)
public class MixinSyncLockOnCamera {

    @Shadow private float xRot;
    @Shadow private float yRot;

    @Unique private static boolean wasLockedOn;
    @Unique private static float lastPitch;
    @Unique private static int blendFrames;

    @Inject(method = "calcRotations", at = @At("HEAD"))
    private void onCalcRotations(Entity cameraEntity, float partialTick, CallbackInfoReturnable<?> cir) {
        boolean lockedOn = EpicFightHelper.isLockOnTargeting();

        if (lockedOn) {
            // bypass ComputeCameraAngles circular dependency on NeoForge
            this.xRot = EpicFightHelper.getLockOnXRot(partialTick);
            this.yRot = EpicFightHelper.getLockOnYRot(partialTick);
            lastPitch = this.xRot;
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
            this.xRot = lastPitch + (this.xRot - lastPitch) * t;
            blendFrames--;
        }
    }
}
