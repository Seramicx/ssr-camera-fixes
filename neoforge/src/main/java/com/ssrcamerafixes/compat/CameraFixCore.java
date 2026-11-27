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
        if (player != null && !AimRules.isFreeLooking()) {
            boolean useTurn = AimRules.useTurnAllowed();
            if (EpicFightHelper.isAttackKeyActive() && AimRules.attackTurnAllowed()) return true;
            if (useTurn && (EpicFightHelper.isAiming(player) || EpicFightHelper.isHoldingSkill(player) || player.isUsingItem() || player.isBlocking())) return true;
            if (useTurn && (TaczHelper.isAimingOrFiring() || SuperbWarfareHelper.isAimingOrFiring() || ConfluenceHelper.isGunFiringOrAiming(player))) return true;
        }
        if (player != null
                && player.isSprinting()
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

    public static Vec3 cycleOffset(Vec3 current) {
        if (ShoulderCycleHandler.getMode() != ShoulderCycleHandler.Mode.OVERHEAD) {
            return new Vec3(ShoulderCycleHandler.applySide(current.x), current.y, current.z);
        }
        // SSR zeroes x/y when looking down (center_camera_when_looking_down), so don't early-out
        return new Vec3(0.0, overheadY(), current.z);
    }

    public static Vec3 lockOnOffset(Vec3 defaultOffset) {
        if (!EpicFightHelper.isLockOnTargeting()) {
            return null;
        }
        if (ShoulderCycleHandler.getMode() == ShoulderCycleHandler.Mode.OVERHEAD) {
            return new Vec3(0.0, overheadY(), defaultOffset.z);
        }
        return new Vec3(ShoulderCycleHandler.applySide(defaultOffset.x), defaultOffset.y, defaultOffset.z);
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
