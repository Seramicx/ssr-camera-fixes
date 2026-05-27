package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.SsrCameraFixesMod;
import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfingCamera;
import com.github.exopandora.shouldersurfing.api.client.ShoulderSurfing;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = SsrCameraFixesMod.MODID, value = Dist.CLIENT)
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
