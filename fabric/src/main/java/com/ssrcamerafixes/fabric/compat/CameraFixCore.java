package com.ssrcamerafixes.fabric.compat;

import com.ssrcamerafixes.SsrCameraFixesConfig;
import com.ssrcamerafixes.SsrCameraFixesConfig.IdleBehavior;
import com.ssrcamerafixes.fabric.handler.ShoulderCycleHandler;
import com.ssrcamerafixes.fabric.handler.SprintRotateHandler;
import com.ssrcamerafixes.fabric.handler.WalkStopFaceCameraHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public final class CameraFixCore {

    private CameraFixCore() {}

    public static boolean forceVanillaInput() {
        if (SprintRotateHandler.isActive()) {
            return true;
        }
        if (WalkStopFaceCameraHandler.isActive()) {
            return true;
        }
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && (player.isUsingItem() || player.isBlocking())) {
            return true;
        }
        if (WizardsHelper.isCastingLive()) {
            return true;
        }
        if (player != null
                && player.isSprinting()
                && Minecraft.getInstance().options.getCameraType() == CameraType.THIRD_PERSON_BACK
                && idleMode() != IdleBehavior.DECOUPLED) {
            return true;
        }
        return false;
    }

    public static Vec3 overheadOffset(Vec3 current) {
        if (ShoulderCycleHandler.getMode() != ShoulderCycleHandler.Mode.OVERHEAD) {
            return current;
        }
        double overheadY;
        try {
            overheadY = SsrCameraFixesConfig.CAMERA_OVERHEAD_OFFSET_Y.get();
        } catch (Throwable t) {
            overheadY = 1.2;
        }
        return new Vec3(0.0, overheadY, current.z);
    }

    private static IdleBehavior idleMode() {
        try {
            return SsrCameraFixesConfig.IDLE_BEHAVIOR.get();
        } catch (Throwable t) {
            return IdleBehavior.DECOUPLED;
        }
    }
}
