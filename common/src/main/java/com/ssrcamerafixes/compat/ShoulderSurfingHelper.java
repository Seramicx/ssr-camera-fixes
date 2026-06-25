package com.ssrcamerafixes.compat;

import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfingCamera;
import com.github.exopandora.shouldersurfing.api.client.ShoulderSurfing;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public final class ShoulderSurfingHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static volatile Method lookAtCrosshairMethod;
    private static volatile boolean lookAtCrosshairResolved;

    private ShoulderSurfingHelper() {}

    public static boolean isShoulderSurfingActive() {
        try {
            return ShoulderSurfing.getInstance().isShoulderSurfing();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isCameraDecoupled() {
        try {
            IShoulderSurfing ssr = ShoulderSurfing.getInstance();
            return ssr.isShoulderSurfing() && ssr.isCameraDecoupled();
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

    public static void lookAtCrosshairTarget() {
        try {
            IShoulderSurfing ssr = ShoulderSurfing.getInstance();
            if (ssr == null) return;
            if (!lookAtCrosshairResolved) {
                synchronized (ShoulderSurfingHelper.class) {
                    if (!lookAtCrosshairResolved) {
                        try {
                            lookAtCrosshairMethod = ssr.getClass().getMethod("lookAtCrosshairTarget");
                        } catch (NoSuchMethodException e) {
                            LOGGER.debug("SSR lookAtCrosshairTarget not found on {}", ssr.getClass().getName());
                        }
                        lookAtCrosshairResolved = true;
                    }
                }
            }
            Method m = lookAtCrosshairMethod;
            if (m != null) m.invoke(ssr);
        } catch (Throwable ignored) {}
    }

    public static double getStoredShoulderX() {
        try {
            return ShoulderSurfing.getInstance().getClientConfig().getOffsetX();
        } catch (Throwable t) {
            return 0.0;
        }
    }

    public static double getConfigOffsetX() {
        try {
            return ShoulderSurfing.getInstance().getClientConfig().getOffsetX();
        } catch (Throwable t) {
            return 0.0;
        }
    }

    public static double getConfigOffsetY() {
        try {
            return ShoulderSurfing.getInstance().getClientConfig().getOffsetY();
        } catch (Throwable t) {
            return 0.0;
        }
    }

    public static Vec3 getTargetOffset() {
        try {
            IShoulderSurfingCamera cam = ShoulderSurfing.getInstance().getCamera();
            if (cam != null) {
                Vec3 to = cam.getTargetOffset();
                if (to != null) return to;
            }
        } catch (Throwable ignored) {}
        return Vec3.ZERO;
    }
}
