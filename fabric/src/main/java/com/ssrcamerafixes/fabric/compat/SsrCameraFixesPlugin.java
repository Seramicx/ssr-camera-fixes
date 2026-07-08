package com.ssrcamerafixes.fabric.compat;

import com.github.exopandora.shouldersurfing.api.client.event.ComputeTargetCameraOffsetEvent;
import com.github.exopandora.shouldersurfing.api.client.event.handler.ComputeTargetCameraOffsetEventHandler;
import com.github.exopandora.shouldersurfing.api.client.event.handler.ForceVanillaPlayerInputEventHandler;
import com.github.exopandora.shouldersurfing.api.event.IEventBus;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingPlugin;
import com.ssrcamerafixes.SsrCameraFixesConfig;
import com.ssrcamerafixes.SsrCameraFixesConfig.IdleBehavior;
import com.ssrcamerafixes.fabric.handler.ShoulderCycleHandler;
import com.ssrcamerafixes.fabric.handler.SprintRotateHandler;
import com.ssrcamerafixes.fabric.handler.WalkStopFaceCameraHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class SsrCameraFixesPlugin implements IShoulderSurfingPlugin {

    @Override
    public void register(IEventBus eventBus) {
        eventBus.register(5000, (ComputeTargetCameraOffsetEventHandler) SsrCameraFixesPlugin::overheadOffset);
        eventBus.register((ForceVanillaPlayerInputEventHandler) event -> {
            if (!event.getResult() && isForcingVanillaInput()) {
                event.setResult(true);
            }
        });
    }

    private static void overheadOffset(ComputeTargetCameraOffsetEvent event) {
        if (ShoulderCycleHandler.getMode() != ShoulderCycleHandler.Mode.OVERHEAD) {
            return;
        }
        Vec3 result = event.getResult();
        if (Math.abs(result.x) < 1.0E-4 && Math.abs(result.y) < 1.0E-4) {
            return;
        }
        double overheadY;
        try {
            overheadY = SsrCameraFixesConfig.CAMERA_OVERHEAD_OFFSET_Y.get();
        } catch (Exception e) {
            overheadY = 1.2;
        }
        event.setResult(new Vec3(0.0, overheadY, result.z));
    }

    private static boolean isForcingVanillaInput() {
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

    private static IdleBehavior idleMode() {
        try {
            return SsrCameraFixesConfig.IDLE_BEHAVIOR.get();
        } catch (Throwable t) {
            return IdleBehavior.DECOUPLED;
        }
    }
}
