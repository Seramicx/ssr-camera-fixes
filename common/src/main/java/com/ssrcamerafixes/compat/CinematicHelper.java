package com.ssrcamerafixes.compat;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public final class CinematicHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static Method active;
    private static boolean resolved = false;

    private CinematicHelper() {}

    private static void resolve() {
        if (resolved) return;
        resolved = true;
        if (!CompatSwitches.enabled("cinematic")) return;
        try {
            active = Class.forName("dev.dyslexic.cinematic.client.Scene").getMethod("active");
        } catch (Throwable t) {
            LOGGER.debug("Cinematic Interactions scene state unavailable: {}", t.toString());
        }
    }

    public static boolean isSceneActive() {
        resolve();
        if (active == null) return false;
        try {
            return (Boolean) active.invoke(null);
        } catch (Throwable t) {
            return false;
        }
    }
}
