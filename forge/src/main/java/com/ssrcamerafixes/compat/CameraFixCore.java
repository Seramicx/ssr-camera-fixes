package com.ssrcamerafixes.compat;

import com.ssrcamerafixes.SsrCameraFixesConfig;
import com.ssrcamerafixes.SsrCameraFixesConfig.IdleBehavior;
import com.ssrcamerafixes.handler.ShoulderCycleHandler;
import com.ssrcamerafixes.handler.SprintRotateHandler;
import com.ssrcamerafixes.handler.WalkStopFaceCameraHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class CameraFixCore {

    private CameraFixCore() {}

    public static boolean forceVanillaInput() {
        if (EpicFightHelper.isLockOnTargeting()) {
            return true;
        }
        if (SprintRotateHandler.isActive()) {
            return true;
        }
        if (WalkStopFaceCameraHandler.isActive()) {
            return true;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null
                && (EpicFightHelper.isAiming(player)
                        || EpicFightHelper.isAttackKeyActive()
                        || EpicFightHelper.isHoldingSkill(player)
                        || player.isBlocking())) {
            return true;
        }
        if (TaczHelper.isAimingOrFiring()) {
            return true;
        }
        if (SuperbWarfareHelper.isAimingOrFiring()) {
            return true;
        }
        if (player != null
                && player.isSprinting()
                && !player.isInWater()
                && Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_BACK
                && idleMode() != IdleBehavior.DECOUPLED) {
            return true;
        }
        return false;
    }

    public static boolean needsCameraCoupling() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && EpicFightHelper.needsCameraCoupling(player);
    }

    public static Vec3 overheadOffset(Vec3 current) {
        if (ShoulderCycleHandler.getMode() != ShoulderCycleHandler.Mode.OVERHEAD) {
            return current;
        }
        // Always apply — SSR may zero x/y when looking down (center_camera_when_looking_down),
        // and the old early-out made OVERHEAD look identical to a centered shoulder.
        return new Vec3(0.0, overheadY(), current.z);
    }

    public static Vec3 lockOnOffset(Vec3 defaultOffset) {
        if (!EpicFightHelper.isLockOnTargeting()) {
            return null;
        }
        if (ShoulderCycleHandler.getMode() == ShoulderCycleHandler.Mode.OVERHEAD) {
            return new Vec3(0.0, overheadY(), defaultOffset.z);
        }
        return defaultOffset;
    }

    public static boolean vsPreserveDefault() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && ValkyrienSkiesHelper.isMountedOnShip(player);
    }

    private static double overheadY() {
        try {
            return SsrCameraFixesConfig.CAMERA_OVERHEAD_OFFSET_Y.get();
        } catch (Throwable t) {
            return 1.2;
        }
    }

    private static IdleBehavior idleMode() {
        try {
            return SsrCameraFixesConfig.IDLE_BEHAVIOR.get();
        } catch (Throwable t) {
            return IdleBehavior.DECOUPLED;
        }
    }
}
