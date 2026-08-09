package com.ssrcamerafixes.mixin;

import com.github.exopandora.shouldersurfing.api.math.Vec2f;
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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// While locked on, leave aiming to EF. On unlock freeze at the last lock-on facing or renderTick
// follows the player and snaps the view
@Pseudo
@Mixin(value = ShoulderSurfingCamera.class, remap = false)
public class MixinSyncLockOnCameraV5 {

    @Shadow private Vec2f renderRotation;

    @Unique private static boolean wasLockedOn;
    @Unique private static int holdFrames;

    @Inject(method = "renderTick", at = @At("TAIL"), require = 0, remap = false)
    private void ssrcamerafixes$syncLockOn(Entity cameraEntity, float partialTick, CallbackInfo ci) {
        boolean lockedOn = EpicFightHelper.isLockOnTargeting();

        if (lockedOn) {
            LockOnSsrAim.rememberFacing(this.renderRotation.x(), this.renderRotation.y());
            wasLockedOn = true;
            holdFrames = 0;
            return;
        }

        if (wasLockedOn) {
            if (LockOnSsrAim.hasLastFacing()) {
                this.renderRotation = new Vec2f(LockOnSsrAim.lastPitch(), LockOnSsrAim.lastYaw());
            }
            holdFrames = 3;
            wasLockedOn = false;
            return;
        }

        if (holdFrames > 0 && LockOnSsrAim.hasLastFacing()) {
            this.renderRotation = new Vec2f(LockOnSsrAim.lastPitch(), LockOnSsrAim.lastYaw());
            holdFrames--;
        }
    }
}
