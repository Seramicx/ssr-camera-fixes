package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.SableRagdollHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

// Sable Ragdolls saves and restores the vanilla camera type, which brings shoulder surfing back as plain third person
public final class SableRagdollCameraHandler {

    public static final SableRagdollCameraHandler INSTANCE = new SableRagdollCameraHandler();

    private boolean wasSwitched = false;
    private boolean ssrO = false;
    private boolean restoreSsr = false;
    private int restoreTicks;

    private SableRagdollCameraHandler() {}

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        boolean switched = SableRagdollHelper.isCameraSwitched();

        if (switched && !wasSwitched) {
            restoreSsr = ssrO;
        } else if (!switched && wasSwitched) {
            restoreTicks = restoreSsr ? 5 : 0;
            restoreSsr = false;
        }

        if (restoreTicks > 0) {
            restoreTicks--;
            if (ShoulderSurfingHelper.isThirdPersonBackPerspective()) {
                ShoulderSurfingHelper.changeToShoulderSurfing();
                restoreTicks = 0;
            }
        }

        ssrO = ShoulderSurfingHelper.isShoulderSurfingPerspective();
        wasSwitched = switched;
    }
}
