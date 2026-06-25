package com.ssrcamerafixes.fabric1211.compat;

import com.github.exopandora.shouldersurfing.api.client.event.ComputeTargetCameraOffsetEvent;
import com.github.exopandora.shouldersurfing.api.client.event.ForceVanillaPlayerInputEvent;
import com.github.exopandora.shouldersurfing.api.event.IEventBus;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingPlugin;
import com.ssrcamerafixes.SsrCameraFixesConfig;
import com.ssrcamerafixes.SsrCameraFixesConfig.IdleBehavior;
import com.ssrcamerafixes.compat.WizardsHelper;
import com.ssrcamerafixes.fabric1211.handler.ShoulderCycleHandler;
import com.ssrcamerafixes.fabric1211.handler.SprintRotateHandler;
import com.ssrcamerafixes.fabric1211.handler.WalkStopFaceCameraHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

public class SsrCameraFixesPlugin implements IShoulderSurfingPlugin {

    @Override
    public void register(IEventBus eventBus) {
        eventBus.register(5000, SsrCameraFixesPlugin::applyOverheadOffset);
        eventBus.register(SsrCameraFixesPlugin::forceVanillaInput);
    }

    private static void applyOverheadOffset(ComputeTargetCameraOffsetEvent event) {
        if (ShoulderCycleHandler.getMode() != ShoulderCycleHandler.Mode.OVERHEAD) {
            return;
        }
        Vec3 result = event.getResult();
        if (Math.abs(result.x) < 1.0E-4 && Math.abs(result.y) < 1.0E-4) {
            return;
        }
        event.setResult(new Vec3(0.0, overheadY(), result.z));
    }

    private static void forceVanillaInput(ForceVanillaPlayerInputEvent event) {
        if (event.getResult()) {
            return;
        }
        if (SprintRotateHandler.isActive() || WalkStopFaceCameraHandler.isActive()) {
            event.setResult(true);
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player != null && (player.isUsingItem() || player.isBlocking())) {
            event.setResult(true);
            return;
        }
        if (WizardsHelper.isCasting()) {
            event.setResult(true);
            return;
        }
        if (player != null
                && player.isSprinting()
                && mc.options.getCameraType() == CameraType.THIRD_PERSON_BACK
                && idleMode() != IdleBehavior.DECOUPLED) {
            event.setResult(true);
        }
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
