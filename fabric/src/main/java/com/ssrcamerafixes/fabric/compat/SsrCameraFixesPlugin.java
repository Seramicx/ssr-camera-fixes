package com.ssrcamerafixes.fabric.compat;

import com.github.exopandora.shouldersurfing.api.client.event.ComputeTargetCameraOffsetEvent;
import com.github.exopandora.shouldersurfing.api.client.event.handler.ComputePlayerAimStateEventHandler;
import com.github.exopandora.shouldersurfing.api.client.event.handler.ComputeTargetCameraOffsetEventHandler;
import com.github.exopandora.shouldersurfing.api.client.event.handler.ForceVanillaPlayerInputEventHandler;
import com.github.exopandora.shouldersurfing.api.event.IEventBus;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingPlugin;
import com.ssrcamerafixes.compat.HeldCrossbowAim;

public class SsrCameraFixesPlugin implements IShoulderSurfingPlugin {

    @Override
    public void register(IEventBus eventBus) {
        eventBus.register(5000, (ComputeTargetCameraOffsetEventHandler) SsrCameraFixesPlugin::overheadOffset);
        eventBus.register((ForceVanillaPlayerInputEventHandler) event -> {
            if (!event.getResult() && CameraFixCore.forceVanillaInput()) {
                event.setResult(true);
            }
        });
        // Above the builtin's 1000 so this overwrites its "charged" match.
        eventBus.register(5000, (ComputePlayerAimStateEventHandler) event -> {
            if (event.getResult() && HeldCrossbowAim.isPassiveHold(event.getEntity())) {
                event.setResult(false);
            }
        });
    }

    private static void overheadOffset(ComputeTargetCameraOffsetEvent event) {
        event.setResult(CameraFixCore.overheadOffset(event.getResult()));
    }
}
