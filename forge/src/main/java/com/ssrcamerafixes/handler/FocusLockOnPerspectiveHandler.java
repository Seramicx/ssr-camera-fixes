package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.FocusHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// Shoulder-surf and Focus's lock-on camera fight, which vanilla third-person avoids; swap to it for a lock-on
// that starts from shoulder surfing. Other views are left to Focus, which already handles first person itself.
public final class FocusLockOnPerspectiveHandler {

    public static final FocusLockOnPerspectiveHandler INSTANCE = new FocusLockOnPerspectiveHandler();

    private boolean wasLockedOn = false;
    private boolean tookOver = false;

    private FocusLockOnPerspectiveHandler() {}

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        boolean lockedOn = FocusHelper.isLockedOn();

        if (lockedOn && !wasLockedOn) {
            if (ShoulderSurfingHelper.isShoulderSurfingPerspective()) {
                tookOver = true;
                ShoulderSurfingHelper.changeToThirdPersonBack();
            }
        } else if (!lockedOn && wasLockedOn) {
            // Only restore if we still own the view; if the player cycled away during lock-on, leave their choice
            if (tookOver && ShoulderSurfingHelper.isThirdPersonBackPerspective()) {
                ShoulderSurfingHelper.changeToShoulderSurfing();
            }
            tookOver = false;
        }

        wasLockedOn = lockedOn;
    }
}
