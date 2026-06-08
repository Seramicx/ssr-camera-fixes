package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.SsrCameraFixesMod;
import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfingCamera;
import com.github.exopandora.shouldersurfing.api.client.ShoulderSurfing;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SsrCameraFixesMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class LockOnCameraSyncHandler {

    private static boolean wasLockedOn = false;

    private LockOnCameraSyncHandler() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        boolean lockedOn = EpicFightHelper.isLockOnTargeting();

        if (!lockedOn) {
            if (wasLockedOn && ShoulderSurfingHelper.isShoulderSurfingActive()) {
                IShoulderSurfingCamera cam = ShoulderSurfing.getInstance().getCamera();
                if (cam != null) {
                    cam.setYRot((float) event.getYaw());
                    cam.setXRot((float) event.getPitch());
                }
            }
            wasLockedOn = false;
            return;
        }

        wasLockedOn = true;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;

        IShoulderSurfingCamera cam = ShoulderSurfing.getInstance().getCamera();
        if (cam == null) return;
        cam.setYRot((float) event.getYaw());
        cam.setXRot((float) event.getPitch());
    }
}
