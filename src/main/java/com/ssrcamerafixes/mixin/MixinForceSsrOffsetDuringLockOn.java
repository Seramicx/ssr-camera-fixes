package com.ssrcamerafixes.mixin;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera;
import com.github.exopandora.shouldersurfing.config.Config;
import com.ssrcamerafixes.SsrCameraFixesConfig;
import com.ssrcamerafixes.handler.ShoulderCycleHandler;
import com.ssrcamerafixes.compat.EpicFightHelper;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


/**
 * Forces SSR's {@code targetOffset} field to the user's configured shoulder
 * offset during EpicFight lock-on, fixing two related bugs:
 *
 * <ol>
 *   <li><b>X offset zeroed during lock-on.</b> SSR's
 *       {@code ShoulderSurfingCamera.calcOffset} computes a target offset,
 *       applies modifiers / clipping / callbacks, and stores the result in
 *       {@code this.targetOffset}. During lock-on with the full BLO + EpicFight
 *       stack, that final {@code targetOffset.x} comes out as 0 - the
 *       standard shoulder shift is gone. The exact culprit inside SSR's chain
 *       is unclear; this mixin sidesteps the question by overwriting the
 *       field after all internal modifications run.</li>
 *
 *   <li><b>~2 second centered camera after lock-off.</b> SSR's
 *       {@code this.offset} (smoothed, lerped per tick toward
 *       {@code this.targetOffset}) drifts to the zeroed-X value during
 *       lock-on. Post-lock-off the camera renders directly from
 *       {@code this.offset}, so the X needs to lerp back from 0 to config_x.
 *       By keeping {@code this.targetOffset} pinned at config_x throughout
 *       lock-on, SSR's per-tick lerp converges {@code this.offset} to
 *       config_x during the lock-on itself. When lock-on ends,
 *       {@code this.offset} is already correct.</li>
 * </ol>
 *
 * <p>Honors our shoulder cycle's {@code OVERHEAD} mode (overrides X to 0 and
 * Y to {@code SsrCameraFixesConfig.CAMERA_OVERHEAD_OFFSET_Y}) so the cycle
 * still works during lock-on.
 *
 * <p>We deliberately leave {@code this.offset} and {@code this.offsetO} alone
 * so SSR's per-tick lerping (in {@code ShoulderSurfingCamera.tick} at
 * {@code transition_speed_multiplier}, default 0.25) produces smooth preset
 * transitions during lock-on, matching the non-lock-on feel.
 *
 * <p>Injects after {@code INVOKE calcCameraDrag} (same point BLO's compat
 * uses, but at {@code AFTER} shift) so we run after BLO's compat potentially
 * modifies {@code this.targetOffset}.
 */
@Mixin(value = ShoulderSurfingCamera.class, remap = false)
public abstract class MixinForceSsrOffsetDuringLockOn {

    @Shadow private Vec3 targetOffset;

    @Inject(
        method = "calcOffset",
        at = @At(
            value = "INVOKE",
            target = "Lcom/github/exopandora/shouldersurfing/client/ShoulderSurfingCamera;calcCameraDrag(Lnet/minecraft/client/Camera;Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/world/phys/Vec3;",
            shift = At.Shift.AFTER
        ),
        require = 0,
        remap = false
    )
    private void ssrcamerafixes$forceConfigOffsetDuringLockOn(
        Camera camera,
        BlockGetter level,
        float partialTick,
        Entity cameraEntity,
        CallbackInfoReturnable<Vec3> cir
    ) {
        if (!EpicFightHelper.isLockOnTargeting()) return;

        double offX = Config.CLIENT.getOffsetX();
        double offY = Config.CLIENT.getOffsetY();
        double offZ = Config.CLIENT.getOffsetZ();
        if (ShoulderCycleHandler.getMode() == ShoulderCycleHandler.Mode.OVERHEAD) {
            offX = 0.0;
            try {
                offY = SsrCameraFixesConfig.CAMERA_OVERHEAD_OFFSET_Y.get();
            } catch (Throwable t) {
                offY = 1.2;
            }
        }

        this.targetOffset = new Vec3(offX, offY, offZ);
    }
}
