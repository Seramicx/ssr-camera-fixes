package com.ssrcamerafixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


/**
 * Disables EpicFight 20.14+'s built-in {@code ShoulderSurfingCompat.lockOnTick}
 * listener.
 *
 * <p>The bug it caused: EpicFight ships its own SSR plugin. Its
 * {@code lockOnTick} method listens to {@code LockOnEvent.Tick} (fired from
 * Better Lockon's {@code rewroteClientTick} during the lock-on lerp) and
 * writes {@code EpicFightCameraAPI.setCameraRotations(...)} every tick using
 * {@code SSR_camera.getYRot() + lerp_toward_target}. Because SSR's camera
 * yRot follows {@code player.yRot} via its {@code followPlayerRotations}
 * feature, and BLO sets {@code player.yRot} to body/movement direction
 * during sprint+lock-on (for the orbital sprint feel), SSR's cam.yRot
 * tracks movement direction. EpicFight's {@code lockOnTick} then overwrites
 * the cameraYRot value BLO just lerped toward the target with one based on
 * movement direction. Net effect: cameraYRot oscillates between target
 * (BLO's write) and movement direction (EpicFight's write), with movement
 * direction winning since it runs last each tick.
 *
 * <p>Cancelling {@code lockOnTick} at HEAD lets BLO's lerp proceed
 * unopposed; cameraYRot tracks target normally. EpicFight's other compat
 * piece ({@code buildCameraTransform}, which feeds the lerped cameraYRot
 * into SSR's camera state for rendering) still runs and now propagates the
 * correct target-tracking yRot.
 *
 * <p>{@code @Pseudo} because the target class is in EpicFight, which is on
 * the runtime classpath but not at compile time.
 */
@Pseudo
@Mixin(targets = "yesman.epicfight.compat.ShoulderSurfingCompat", remap = false)
public abstract class MixinDisableEpicFightSsrLockOnTick {

    @Inject(method = "lockOnTick", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void ssrcamerafixes$cancelEpicFightSsrLockOnTick(Object event, CallbackInfo ci) {
        ci.cancel();
    }
}
