package com.ssrcamerafixes.compat;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public final class FocusHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static Method getLockedTarget = null;
    private static boolean resolved = false;

    private FocusHelper() {}

    private static void resolve() {
        if (resolved) return;
        resolved = true;
        try {
            Class<?> handler = Class.forName("com.jvn.focus.client.LockOnHandler");
            getLockedTarget = handler.getMethod("getLockedTarget");
        } catch (Throwable t) {
            LOGGER.debug("Focus LockOnHandler reflection unavailable: {}", t.toString());
        }
    }

    public static boolean isLockedOn() {
        resolve();
        if (getLockedTarget == null) return false;
        try {
            return getLockedTarget.invoke(null) != null;
        } catch (Throwable t) {
            return false;
        }
    }
}
