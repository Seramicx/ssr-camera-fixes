package com.ssrcamerafixes.mixin;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera;
import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.LockOnSsrAim;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// While locked on, leave aiming to EF lockOnTick + syncLockOnRotations (once per tick, no per-frame
// ease). On unlock, freeze at the last lock-on facing
@Pseudo
@Mixin(value = ShoulderSurfingCamera.class, remap = false)
public class MixinSyncLockOnCameraV4 {

    @Shadow private float xRot;
    @Shadow private float yRot;

    @Unique private static boolean wasLockedOn;
    @Unique private static int holdFrames;

    @Inject(method = "calcRotations", at = @At("HEAD"), require = 0, remap = false)
    private void ssrcamerafixes$syncLockOn(Entity cameraEntity, float partialTick, CallbackInfoReturnable<?> cir) {
        boolean lockedOn = EpicFightHelper.isLockOnTargeting();

        if (lockedOn) {
            // EF already wrote SSR-aware angles; just keep an unlock snapshot.
            LockOnSsrAim.rememberFacing(this.xRot, this.yRot);
            wasLockedOn = true;
            holdFrames = 0;
            return;
        }

        if (wasLockedOn) {
            if (LockOnSsrAim.hasLastFacing()) {
                this.xRot = LockOnSsrAim.lastPitch();
                this.yRot = LockOnSsrAim.lastYaw();
            }
            holdFrames = 3;
            wasLockedOn = false;
            return;
        }

        if (holdFrames > 0 && LockOnSsrAim.hasLastFacing()) {
            this.xRot = LockOnSsrAim.lastPitch();
            this.yRot = LockOnSsrAim.lastYaw();
            holdFrames--;
        }
    }
}
