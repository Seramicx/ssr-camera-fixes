package com.ssrcamerafixes.compat;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class PunchyHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static Method isActive = null;
    private static Method standDown = null;
    private static Field activatedByPunchy = null;
    private static boolean resolved = false;

    private PunchyHelper() {}

    private static void resolve() {
        if (resolved) return;
        resolved = true;
        if (!CompatSwitches.enabled("punchymoves")) return;
        try {
            Class<?> controller = Class.forName("punchymoves.client.camera.ShoulderCameraController");
            isActive = controller.getMethod("isActive");
            standDown = controller.getMethod("onVanillaPerspectiveChange");
            activatedByPunchy = controller.getDeclaredField("activatedByPunchy");
            activatedByPunchy.setAccessible(true);
        } catch (Throwable t) {
            LOGGER.debug("Punchy Moves camera reflection unavailable: {}", t.toString());
        }
    }

    public static boolean isCameraActive() {
        resolve();
        if (isActive == null) return false;
        try {
            return (boolean) isActive.invoke(null);
        } catch (Throwable t) {
            return false;
        }
    }

    // true from Punchy's own toggle until something turns it off; isCameraActive also needs third person back
    public static boolean isCameraSwitchedOn() {
        resolve();
        if (activatedByPunchy == null) return false;
        try {
            return activatedByPunchy.getBoolean(null);
        } catch (Throwable t) {
            return false;
        }
    }

    public static void standDown() {
        resolve();
        if (standDown == null) return;
        try {
            standDown.invoke(null);
        } catch (Throwable t) {
            LOGGER.debug("Punchy Moves camera stand-down failed: {}", t.toString());
        }
    }
}
