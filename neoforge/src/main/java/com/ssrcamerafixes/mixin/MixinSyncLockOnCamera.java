package com.ssrcamerafixes.mixin;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera;
import com.github.exopandora.shouldersurfing.math.Vec2f;
import com.ssrcamerafixes.handler.LockOnCameraSyncHandler;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ShoulderSurfingCamera.class, remap = false)
public abstract class MixinSyncLockOnCamera {

    @Shadow private float yRot;
    @Shadow private float xRot;

    @Inject(method = "calcRotations", at = @At("HEAD"), require = 0, remap = false)
    private void ssrcamerafixes$syncBeforeCalcRotations(Entity cameraEntity, float partialTick, CallbackInfoReturnable<Vec2f> cir) {
        float yaw = LockOnCameraSyncHandler.getCachedYaw();
        if (Float.isNaN(yaw)) return;
        this.yRot = yaw;
        this.xRot = LockOnCameraSyncHandler.getCachedPitch();
    }
}
