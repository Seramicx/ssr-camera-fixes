package com.ssrcamerafixes.compat;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.lang.reflect.Field;

public final class SableRagdollHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static Field cameraSwitched = null;
    private static boolean resolved = false;

    private SableRagdollHelper() {}

    private static void resolve() {
        if (resolved) return;
        resolved = true;
        if (!CompatSwitches.enabled("sableplayerragdoll")) return;
        try {
            Class<?> helper = Class.forName("dev.leo.sableplayerragdoll.neoforge.client.RagdollCameraHelper");
            cameraSwitched = helper.getDeclaredField("cameraSwitched");
            cameraSwitched.setAccessible(true);
        } catch (Throwable t) {
            LOGGER.debug("Sable Ragdolls camera reflection unavailable: {}", t.toString());
        }
    }

    public static boolean isCameraSwitched() {
        resolve();
        if (cameraSwitched == null) return false;
        try {
            return cameraSwitched.getBoolean(null);
        } catch (Throwable t) {
            return false;
        }
    }
}
