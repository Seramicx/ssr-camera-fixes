package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.PunchyHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// Punchy Moves has its own shoulder camera and both claim third person back; whichever was switched on last keeps it
public final class PunchyCameraHandler {

    public static final PunchyCameraHandler INSTANCE = new PunchyCameraHandler();

    private boolean wasOn = false;
    private boolean tookOver = false;
    private int ssrTicks;

    private PunchyCameraHandler() {}

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        boolean ssr = ShoulderSurfingHelper.isShoulderSurfingPerspective();
        ssrTicks = ssr ? ssrTicks + 1 : 0;
        boolean on = PunchyHelper.isCameraSwitchedOn();

        if (on && ssr) {
            // Punchy turns itself back on whenever the view enters third person; only a later switch-on is the player's pick
            if (!wasOn && ssrTicks > 5) {
                tookOver = true;
                ShoulderSurfingHelper.changeToThirdPersonBack();
            } else if (PunchyHelper.isCameraActive()) {
                PunchyHelper.standDown();
                tookOver = false;
                on = false;
            }
        } else if (!on && wasOn) {
            if (tookOver && ShoulderSurfingHelper.isThirdPersonBackPerspective()) {
                ShoulderSurfingHelper.changeToShoulderSurfing();
            }
            tookOver = false;
        }

        wasOn = on;
    }
}
