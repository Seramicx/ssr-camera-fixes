package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class LockOnCameraSyncHandler {

    public static final LockOnCameraSyncHandler INSTANCE = new LockOnCameraSyncHandler();

    private static boolean wasLockedOn = false;
    private static float savedPitch = 0.0F;
    private static int blendFrames = 0;

    private LockOnCameraSyncHandler() {}

    // LOWEST so this mod's camera angle override runs after EpicFight's camera adjustments
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        boolean lockedOn = EpicFightHelper.isLockOnTargeting();
        boolean ssrActive = ShoulderSurfingHelper.isShoulderSurfingActive();

        if (lockedOn) {
            wasLockedOn = true;
            blendFrames = 0;
            if (ssrActive) {
                ShoulderSurfingHelper.setCameraYawPitch((float) event.getYaw(), (float) event.getPitch());
                savedPitch = (float) event.getPitch();
            }
            return;
        }

        if (wasLockedOn) {
            wasLockedOn = false;
            blendFrames = 5;
            if (ssrActive) {
                ShoulderSurfingHelper.setCameraYawPitch((float) event.getYaw(), (float) event.getPitch());
            }
        }

        if (blendFrames > 0 && ssrActive) {
            float t = 1.0F - blendFrames / 5.0F;
            float current = ShoulderSurfingHelper.getCameraXRot();
            float held = savedPitch + (current - savedPitch) * t;
            ShoulderSurfingHelper.setCameraYawPitch(ShoulderSurfingHelper.getCameraYaw(), held);
            blendFrames--;
        }
    }
}
