package com.ssrcamerafixes.compat;

import com.github.exopandora.shouldersurfing.api.client.event.ComputeTargetCameraOffsetEvent;
import com.ssrcamerafixes.SsrCameraFixesConfig;
import com.ssrcamerafixes.handler.ShoulderCycleHandler;
import net.minecraft.world.phys.Vec3;

// EpicFight lock-on pulls the SSR shoulder toward the target; force the raw config offset back so the
// shoulder stays put. getDefaultOffset() is the config camera offset (offsetX/Y/Z) before any modifier.
public final class LockOnOffsetHandler {

    private LockOnOffsetHandler() {}

    public static void apply(ComputeTargetCameraOffsetEvent event) {
        if (!EpicFightHelper.isLockOnTargeting()) {
            return;
        }
        Vec3 configOffset = event.getDefaultOffset();
        if (ShoulderCycleHandler.getMode() == ShoulderCycleHandler.Mode.OVERHEAD) {
            double overheadY;
            try {
                overheadY = SsrCameraFixesConfig.CAMERA_OVERHEAD_OFFSET_Y.get();
            } catch (Throwable t) {
                overheadY = 1.2;
            }
            event.setResult(new Vec3(0.0, overheadY, configOffset.z));
        } else {
            event.setResult(configOffset);
        }
    }
}
