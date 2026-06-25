package com.ssrcamerafixes.handler;

import com.github.exopandora.shouldersurfing.api.client.ShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.model.Perspective;
import com.ssrcamerafixes.compat.FocusHelper;
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
            if (Perspective.current() == Perspective.SHOULDER_SURFING) {
                tookOver = true;
                ShoulderSurfing.getInstance().changePerspective(Perspective.THIRD_PERSON_BACK);
            }
        } else if (!lockedOn && wasLockedOn) {
            // Only restore if we still own the view; if the player cycled away during lock-on, leave their choice
            if (tookOver && Perspective.current() == Perspective.THIRD_PERSON_BACK) {
                ShoulderSurfing.getInstance().changePerspective(Perspective.SHOULDER_SURFING);
            }
            tookOver = false;
        }

        wasLockedOn = lockedOn;
    }
}
