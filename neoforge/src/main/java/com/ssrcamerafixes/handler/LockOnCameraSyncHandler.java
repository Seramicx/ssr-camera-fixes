package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

public final class LockOnCameraSyncHandler {

    public static final LockOnCameraSyncHandler INSTANCE = new LockOnCameraSyncHandler();

    private static boolean wasLockedOn = false;

    private LockOnCameraSyncHandler() {}

    // LOWEST so this mod's camera angle override runs after EpicFight's camera adjustments
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        boolean lockedOn = EpicFightHelper.isLockOnTargeting();

        if (!lockedOn) {
            if (wasLockedOn && ShoulderSurfingHelper.isShoulderSurfingActive()) {
                ShoulderSurfingHelper.setCameraYawPitch((float) event.getYaw(), (float) event.getPitch());
            }
            wasLockedOn = false;
            return;
        }

        wasLockedOn = true;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;

        ShoulderSurfingHelper.setCameraYawPitch((float) event.getYaw(), (float) event.getPitch());
    }
}
