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

    private static float cachedYaw = Float.NaN;
    private static float cachedPitch = Float.NaN;

    private LockOnCameraSyncHandler() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (!EpicFightHelper.isLockOnTargeting()) {
            cachedYaw = Float.NaN;
            cachedPitch = Float.NaN;
            return;
        }
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;

        cachedYaw = (float) event.getYaw();
        cachedPitch = (float) event.getPitch();

        IShoulderSurfingCamera cam = ShoulderSurfing.getInstance().getCamera();
        if (cam == null) return;
        cam.setYRot(cachedYaw);
        cam.setXRot(cachedPitch);
    }

    public static float getCachedYaw() {
        return cachedYaw;
    }

    public static float getCachedPitch() {
        return cachedPitch;
    }
}
