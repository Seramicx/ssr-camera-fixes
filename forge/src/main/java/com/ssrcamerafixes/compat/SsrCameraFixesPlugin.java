package com.ssrcamerafixes.compat;

import com.github.exopandora.shouldersurfing.api.client.event.handler.ComputeCameraCouplingEventHandler;
import com.github.exopandora.shouldersurfing.api.client.event.handler.ComputePlayerAttackStateEventHandler;
import com.github.exopandora.shouldersurfing.api.client.event.handler.ComputeTargetCameraOffsetEventHandler;
import com.github.exopandora.shouldersurfing.api.client.event.handler.ForceVanillaPlayerInputEventHandler;
import com.github.exopandora.shouldersurfing.api.event.IEventBus;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingPlugin;
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
    public void register(IEventBus eventBus) {
        eventBus.register(5000, (ComputeTargetCameraOffsetEventHandler) SsrCameraFixesPlugin::overheadOffset);
        eventBus.register(5100, (ComputeTargetCameraOffsetEventHandler) LockOnOffsetHandler::apply);
        VsDynamicOffsetSkipHandler vsSkip = new VsDynamicOffsetSkipHandler();
        eventBus.register(1999, (ComputeTargetCameraOffsetEventHandler) vsSkip::record);
        eventBus.register(2001, (ComputeTargetCameraOffsetEventHandler) vsSkip::restore);
        eventBus.register((ForceVanillaPlayerInputEventHandler) event -> {
            if (!event.getResult() && isForcingVanillaInput()) {
                event.setResult(true);
            }
        });
        eventBus.register(3000, (ComputeCameraCouplingEventHandler) event -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && EpicFightHelper.needsCameraCoupling(player)) {
                event.setResult(true);
            }
        });
        eventBus.register(3000, (ComputePlayerAttackStateEventHandler) event -> {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && EpicFightHelper.needsCameraCoupling(player)) {
                event.setResult(true);
            }
        });
    }

    private static void overheadOffset(com.github.exopandora.shouldersurfing.api.client.event.ComputeTargetCameraOffsetEvent event) {
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
                        || player.isUsingItem()
                        || player.isBlocking())) {
            return true;
        }
        if (TaczHelper.isAimingOrFiring()) {
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

    private static IdleBehavior idleMode() {
        try {
            return SsrCameraFixesConfig.IDLE_BEHAVIOR.get();
        } catch (Throwable t) {
            return IdleBehavior.DECOUPLED;
        }
    }
}
