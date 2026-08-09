package com.ssrcamerafixes.compat;

import com.github.exopandora.shouldersurfing.api.client.event.ComputeTargetCameraOffsetEvent;
import net.minecraft.world.phys.Vec3;

// EpicFight lock-on pulls the SSR shoulder toward the target; force the raw config offset back so the
// shoulder stays put. getDefaultOffset() is the config camera offset (offsetX/Y/Z) before any modifier.
public final class LockOnOffsetHandler {

    private LockOnOffsetHandler() {}

    public static void apply(ComputeTargetCameraOffsetEvent event) {
        Vec3 result = CameraFixCore.lockOnOffset(event.getDefaultOffset());
        if (result != null) {
            event.setResult(result);
        }
    }
}
