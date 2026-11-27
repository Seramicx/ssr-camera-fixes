package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.CinematicHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// Cinematic Interactions saves the camera type before a scene and restores it after, which brings shoulder surfing back as plain third person
public final class CinematicCameraHandler {

    public static final CinematicCameraHandler INSTANCE = new CinematicCameraHandler();

    private boolean sceneO = false;
    private boolean ssrO = false;
    private boolean ssrBeforeScene = false;
    private int restoreTicks;

    private CinematicCameraHandler() {}

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        boolean scene = CinematicHelper.isSceneActive();

        if (scene && !sceneO) {
            ssrBeforeScene = ssrO;
        } else if (!scene && sceneO) {
            restoreTicks = ssrBeforeScene ? 20 : 0;
        }

        if (restoreTicks > 0) {
            restoreTicks--;
            if (ShoulderSurfingHelper.isShoulderSurfingPerspective()) {
                restoreTicks = 0;
            } else if (ShoulderSurfingHelper.isThirdPersonBackPerspective()) {
                ShoulderSurfingHelper.changeToShoulderSurfing();
                restoreTicks = 0;
            }
        }

        ssrO = ShoulderSurfingHelper.isShoulderSurfingPerspective();
        sceneO = scene;
    }
}
