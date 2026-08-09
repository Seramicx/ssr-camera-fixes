package com.ssrcamerafixes.compat;

import com.github.exopandora.shouldersurfing.api.client.event.ComputeTargetCameraOffsetEvent;
import com.github.exopandora.shouldersurfing.api.client.event.handler.ComputeCameraCouplingEventHandler;
import com.github.exopandora.shouldersurfing.api.client.event.handler.ComputePlayerAimStateEventHandler;
import com.github.exopandora.shouldersurfing.api.client.event.handler.ComputePlayerAttackStateEventHandler;
import com.github.exopandora.shouldersurfing.api.client.event.handler.ComputeTargetCameraOffsetEventHandler;
import com.github.exopandora.shouldersurfing.api.client.event.handler.ForceVanillaPlayerInputEventHandler;
import com.github.exopandora.shouldersurfing.api.event.IEventBus;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingPlugin;

public class SsrCameraFixesPlugin implements IShoulderSurfingPlugin {

    @Override
    public void register(IEventBus eventBus) {
        eventBus.register(5000, (ComputeTargetCameraOffsetEventHandler) SsrCameraFixesPlugin::overheadOffset);
        // After SSR builtins (<=4000). Must win on VS ships or world-space dynamic offset + VS camera basis
        // jitter the shoulder while turning. Lock-on stays after this so EF lock-on still sticks.
        eventBus.register(9000, (ComputeTargetCameraOffsetEventHandler) SsrCameraFixesPlugin::vsShipOffset);
        eventBus.register(9100, (ComputeTargetCameraOffsetEventHandler) LockOnOffsetHandler::apply);
        eventBus.register((ForceVanillaPlayerInputEventHandler) event -> {
            if (!event.getResult() && CameraFixCore.forceVanillaInput()) {
                event.setResult(true);
            }
        });
        eventBus.register(3000, (ComputeCameraCouplingEventHandler) event -> {
            if (CameraFixCore.needsCameraCoupling()) {
                event.setResult(true);
            }
        });
        eventBus.register(3000, (ComputePlayerAttackStateEventHandler) event -> {
            if (CameraFixCore.needsCameraCoupling()) {
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
        // On ships vsShipOffset replaces the result with defaultOffset; still apply overhead there.
        if (CameraFixCore.vsPreserveDefault()) return;
        event.setResult(CameraFixCore.overheadOffset(event.getResult()));
    }

    // On a VS ship SSR's centering + dynamic-offset run in world space and go wacky as the ship rotates,
    // so replace the whole computed offset with the raw config offset (stable). Then apply overhead cycle
    // on top — skipping overhead here made OVERHEAD identical to a centered shoulder on mounts.
    private static void vsShipOffset(ComputeTargetCameraOffsetEvent event) {
        if (CameraFixCore.vsPreserveDefault()) {
            event.setResult(CameraFixCore.overheadOffset(event.getDefaultOffset()));
        }
    }
}
