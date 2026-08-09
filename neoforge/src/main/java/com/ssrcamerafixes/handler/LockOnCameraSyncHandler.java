package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.LockOnSsrAim;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

public final class LockOnCameraSyncHandler {

    public static final LockOnCameraSyncHandler INSTANCE = new LockOnCameraSyncHandler();

    private static boolean wasLockedOn = false;

    private LockOnCameraSyncHandler() {}

    // Never write ComputeCameraAngles into SSR while locked on (NeoForge circular clobber).
    // On unlock, re-apply the last lock-on facing so free-look starts from the enemy view
    // (do not snap to the player's body look).
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        boolean lockedOn = EpicFightHelper.isLockOnTargeting();

        if (!lockedOn && wasLockedOn) {
            LockOnSsrAim.applyLastFacingToSsr();
        }

        wasLockedOn = lockedOn;
    }
}
