package com.ssrcamerafixes.compat;

import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfingCamera;
import com.github.exopandora.shouldersurfing.api.client.ShoulderSurfing;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

/**
 * Thin facade over Shoulder Surfing Reloaded's API. SSR is a hard runtime
 * dependency from this mod's mods.toml, so the imports here are safe.
 *
 * <p>Each public method wraps SSR access in try/catch so this mod survives
 * upstream API churn - a method disappearing returns a benign default
 * rather than crashing the camera.
 */
public final class ShoulderSurfingHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    private ShoulderSurfingHelper() {}

    public static boolean isShoulderSurfingActive() {
        try {
            return ShoulderSurfing.getInstance().isShoulderSurfing();
        } catch (Throwable t) {
            return false;
        }
    }

    public static float getCameraYaw() {
        try {
            if (isShoulderSurfingActive()) {
                IShoulderSurfingCamera cam = ShoulderSurfing.getInstance().getCamera();
                if (cam != null) return cam.getYRot();
            }
        } catch (Throwable ignored) {}
        return Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();
    }

    public static float getCameraXRot() {
        try {
            if (isShoulderSurfingActive()) {
                IShoulderSurfingCamera cam = ShoulderSurfing.getInstance().getCamera();
                if (cam != null) return cam.getXRot();
            }
        } catch (Throwable ignored) {}
        return Minecraft.getInstance().gameRenderer.getMainCamera().getXRot();
    }

    public static void swapShoulder() {
        try {
            IShoulderSurfing ssr = ShoulderSurfing.getInstance();
            if (ssr != null) ssr.swapShoulder();
        } catch (Throwable t) {
            LOGGER.warn("ShoulderSurfingHelper.swapShoulder failed: {}", t.toString());
        }
    }
}
