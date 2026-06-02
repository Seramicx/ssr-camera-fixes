package com.ssrcamerafixes.compat;

import com.github.exopandora.shouldersurfing.api.callback.IPlayerInputCallback;
import com.github.exopandora.shouldersurfing.api.callback.ITargetCameraOffsetCallback;
import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingPlugin;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingRegistrar;
import com.ssrcamerafixes.SsrCameraFixesConfig;
import com.ssrcamerafixes.SsrCameraFixesConfig.IdleBehavior;
import com.ssrcamerafixes.handler.ShoulderCycleHandler;
import com.ssrcamerafixes.handler.SprintRotateHandler;
import com.ssrcamerafixes.handler.WalkStopFaceCameraHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class SsrCameraFixesPlugin implements IShoulderSurfingPlugin {

    @Override
    public void register(IShoulderSurfingRegistrar registrar) {
        registrar.registerTargetCameraOffsetCallback(new OverheadOffsetCallback());
        registrar.registerPlayerInputCallback(new ForceVanillaInputCallback());
    }

    private static final class OverheadOffsetCallback implements ITargetCameraOffsetCallback {
        @Override
        public Vec3 post(IShoulderSurfing instance, Vec3 targetOffset, Vec3 defaultOffset) {
            if (ShoulderCycleHandler.getMode() != ShoulderCycleHandler.Mode.OVERHEAD) {
                return targetOffset;
            }

            if (Math.abs(targetOffset.x) < 1.0E-4 && Math.abs(targetOffset.y) < 1.0E-4) {
                return targetOffset;
            }

            double overheadY;
            try {
                overheadY = SsrCameraFixesConfig.CAMERA_OVERHEAD_OFFSET_Y.get();
            } catch (Exception e) {
                overheadY = 1.2;
            }
            return new Vec3(0.0, overheadY, targetOffset.z);
        }
    }

    private static final class ForceVanillaInputCallback implements IPlayerInputCallback {
        @Override
        public boolean isForcingVanillaMovementInput(IsForcingVanillaMovementInputContext ctx) {
            if (com.ssrcamerafixes.compat.EpicFightHelper.isLockOnTargeting()) {
                return true;
            }
            if (SprintRotateHandler.isActive()) {
                return true;
            }
            if (WalkStopFaceCameraHandler.isActive()) {
                return true;
            }
            Minecraft mc = ctx.minecraft();
            LocalPlayer player = mc != null ? mc.player : null;
            if (player != null
                    && (com.ssrcamerafixes.compat.EpicFightHelper.isAiming(player)
                            || player.isUsingItem()
                            || player.isBlocking())) {
                return true;
            }
            if (TaczHelper.isAimingOrFiring()) {
                return true;
            }
            if (player != null
                    && player.isSprinting()
                    && mc.options.getCameraType() == CameraType.THIRD_PERSON_BACK
                    && idleMode() != IdleBehavior.DECOUPLED) {
                return true;
            }
            return false;
        }

        private static IdleBehavior idleMode() {
            try {
                return SsrCameraFixesConfig.IDLE_BEHAVIOR.get();
            } catch (Throwable t) {
                return IdleBehavior.DECOUPLED;
            }
        }
    }
}
