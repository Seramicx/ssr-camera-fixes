package com.ssrcamerafixes.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ShoulderSurfingHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean resolved;
    private static Method getInstance;
    private static Method isShoulderSurfing;
    private static Method isCameraDecoupled;
    private static Method getCamera;
    private static Method swapShoulder;
    private static Method lookAtCrosshair;
    private static Method getClientConfig;
    private static Method camGetYRot;
    private static Method camGetXRot;
    private static Method camGetTargetOffset;
    private static Method cfgGetOffsetX;
    private static Method cfgGetOffsetY;
    private static Method cfgGetCameraConfig;
    private static Field camRotation;
    private static Field camRotationOffset;
    private static Field camRotationOffsetO;
    private static Constructor<?> vec2fCtor;

    private ShoulderSurfingHelper() {}

    private static synchronized void resolve() {
        if (resolved) return;
        resolved = true;
        try {
            Class<?> iface = Class.forName("com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing");
            try {
                getInstance = iface.getMethod("getInstance");
            } catch (NoSuchMethodException v4) {
                getInstance = Class.forName("com.github.exopandora.shouldersurfing.api.client.ShoulderSurfing").getMethod("getInstance");
            }
            isShoulderSurfing = iface.getMethod("isShoulderSurfing");
            isCameraDecoupled = iface.getMethod("isCameraDecoupled");
            getCamera = iface.getMethod("getCamera");
            swapShoulder = iface.getMethod("swapShoulder");
            getClientConfig = iface.getMethod("getClientConfig");
            Class<?> camIface = Class.forName("com.github.exopandora.shouldersurfing.api.client.IShoulderSurfingCamera");
            camGetYRot = camIface.getMethod("getYRot");
            camGetXRot = camIface.getMethod("getXRot");
            camGetTargetOffset = camIface.getMethod("getTargetOffset");
        } catch (Throwable t) {
            LOGGER.debug("SSR API unavailable: {}", t.toString());
        }
        try {
            Class<?> camImpl = Class.forName("com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera");
            lookAtCrosshair = findLookAtCrosshair();
            try {
                camRotation = camImpl.getDeclaredField("rotation");
                camRotation.setAccessible(true);
                camRotationOffset = camImpl.getDeclaredField("rotationOffset");
                camRotationOffset.setAccessible(true);
                camRotationOffsetO = camImpl.getDeclaredField("rotationOffsetO");
                camRotationOffsetO.setAccessible(true);
                Class<?> vec2f = Class.forName("com.github.exopandora.shouldersurfing.api.math.Vec2f");
                vec2fCtor = vec2f.getConstructor(float.class, float.class);
            } catch (Throwable v4) {
                LOGGER.debug("SSR v5 rotation fields unavailable (v4?): {}", v4.toString());
            }
        } catch (Throwable t) {
            LOGGER.debug("SSR camera impl unavailable: {}", t.toString());
        }
    }

    private static Method findLookAtCrosshair() {
        try {
            return Class.forName("com.github.exopandora.shouldersurfing.client.ShoulderSurfing").getMethod("lookAtCrosshairTarget");
        } catch (Throwable t) {
            return null;
        }
    }

    private static Object instance() {
        if (getInstance == null) return null;
        try {
            return getInstance.invoke(null);
        } catch (Throwable t) {
            return null;
        }
    }

    private static Object camera() {
        Object inst = instance();
        if (inst == null || getCamera == null) return null;
        try {
            return getCamera.invoke(inst);
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean isShoulderSurfingActive() {
        resolve();
        Object inst = instance();
        if (inst == null || isShoulderSurfing == null) return false;
        try {
            return (Boolean) isShoulderSurfing.invoke(inst);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isCameraDecoupled() {
        resolve();
        Object inst = instance();
        if (inst == null || isCameraDecoupled == null) return false;
        try {
            return (Boolean) isShoulderSurfing.invoke(inst) && (Boolean) isCameraDecoupled.invoke(inst);
        } catch (Throwable t) {
            return false;
        }
    }

    public static float getCameraYaw() {
        resolve();
        if (isShoulderSurfingActive()) {
            Object cam = camera();
            if (cam != null && camGetYRot != null) {
                try {
                    return (Float) camGetYRot.invoke(cam);
                } catch (Throwable ignored) {}
            }
        }
        return Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();
    }

    public static float getCameraXRot() {
        resolve();
        if (isShoulderSurfingActive()) {
            Object cam = camera();
            if (cam != null && camGetXRot != null) {
                try {
                    return (Float) camGetXRot.invoke(cam);
                } catch (Throwable ignored) {}
            }
        }
        return Minecraft.getInstance().gameRenderer.getMainCamera().getXRot();
    }

    public static void swapShoulder() {
        resolve();
        Object inst = instance();
        if (inst == null || swapShoulder == null) return;
        try {
            swapShoulder.invoke(inst);
        } catch (Throwable t) {
            LOGGER.warn("ShoulderSurfingHelper.swapShoulder failed: {}", t.toString());
        }
    }

    public static void lookAtCrosshairTarget() {
        resolve();
        Object inst = instance();
        if (inst == null || lookAtCrosshair == null) return;
        try {
            lookAtCrosshair.invoke(inst);
        } catch (Throwable ignored) {}
    }

    public static double getStoredShoulderX() {
        return getConfigOffsetX();
    }

    public static double getConfigOffsetX() {
        return configOffset(true);
    }

    public static double getConfigOffsetY() {
        return configOffset(false);
    }

    private static double configOffset(boolean x) {
        resolve();
        Object inst = instance();
        if (inst == null || getClientConfig == null) return 0.0;
        try {
            Object cfg = getClientConfig.invoke(inst);
            if (cfg == null) return 0.0;
            if (cfgGetCameraConfig == null && cfgGetOffsetX == null) {
                try {
                    cfgGetCameraConfig = cfg.getClass().getMethod("getCameraConfig");
                } catch (NoSuchMethodException v4) {
                    cfgGetOffsetX = cfg.getClass().getMethod("getOffsetX");
                    cfgGetOffsetY = cfg.getClass().getMethod("getOffsetY");
                }
            }
            if (cfgGetCameraConfig != null) {
                Object cam = cfgGetCameraConfig.invoke(cfg);
                Method m = cam.getClass().getMethod(x ? "getOffsetX" : "getOffsetY");
                return (Double) m.invoke(cam);
            }
            return (Double) (x ? cfgGetOffsetX : cfgGetOffsetY).invoke(cfg);
        } catch (Throwable t) {
            return 0.0;
        }
    }

    public static Vec3 getTargetOffset() {
        resolve();
        Object cam = camera();
        if (cam != null && camGetTargetOffset != null) {
            try {
                Object to = camGetTargetOffset.invoke(cam);
                if (to instanceof Vec3 v) return v;
            } catch (Throwable ignored) {}
        }
        return Vec3.ZERO;
    }

    // v5 setYRot/setXRot ignore their argument, so write the camera's rotation fields directly to force a yaw/pitch
    public static void setCameraYawPitch(float yaw, float pitch) {
        resolve();
        Object cam = camera();
        if (cam == null) return;
        try {
            if (camRotation != null && vec2fCtor != null) {
                Object rot = vec2fCtor.newInstance(pitch, yaw);
                Object zero = vec2fCtor.newInstance(0.0F, 0.0F);
                camRotation.set(cam, rot);
                camRotationOffset.set(cam, zero);
                camRotationOffsetO.set(cam, zero);
            }
        } catch (Throwable ignored) {}
    }
}
