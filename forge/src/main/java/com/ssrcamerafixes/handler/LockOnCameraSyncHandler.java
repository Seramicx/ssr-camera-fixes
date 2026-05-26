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
