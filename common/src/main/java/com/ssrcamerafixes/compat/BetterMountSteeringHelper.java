package com.ssrcamerafixes.compat;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class BetterMountSteeringHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean stateResolved;
    private static Method isMountRotateActive;
    private static Method isDecoupleTransitioning;

    private BetterMountSteeringHelper() {}

    private static void resolveState() {
        if (stateResolved) return;
        stateResolved = true;
        try {
            Class<?> handler = Class.forName("com.bettermountsteering.handler.MountSteeringHandler");
            isMountRotateActive = handler.getMethod("isMountRotateActive");
            isDecoupleTransitioning = handler.getMethod("isDecoupleTransitioning");
        } catch (Throwable t) {
            LOGGER.debug("Better Mount Steering state unavailable: {}", t.toString());
        }
    }

    public static boolean isMountRotateActive() {
        resolveState();
        if (isMountRotateActive == null) return false;
        try {
            return (Boolean) isMountRotateActive.invoke(null);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isDecoupleTransitioning() {
        resolveState();
        if (isDecoupleTransitioning == null) return false;
        try {
            return (Boolean) isDecoupleTransitioning.invoke(null);
        } catch (Throwable t) {
            return false;
        }
    }

    /** BMS arms a 500ms latch at CGM fire() for semi-auto; prefer this over raw shoot-key state on mount. */
    public static boolean isGunCombatActive() {
        try {
            Class<?> helper = Class.forName("com.bettermountsteering.compat.GunModHelper");
            Object result = helper.getMethod("isGunFiring").invoke(null);
            return result instanceof Boolean && (Boolean) result;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static void registerCameraSource() {
        try {
            Class<?> sourceIface = Class.forName("com.bettermountsteering.api.MountCameraSource");
            Class<?> api = Class.forName("com.bettermountsteering.api.MountSteeringApi");
            Object proxy = Proxy.newProxyInstance(
                    BetterMountSteeringHelper.class.getClassLoader(),
                    new Class<?>[]{sourceIface},
                    new SsrCameraSource());
            api.getMethod("setCameraSource", sourceIface).invoke(null, proxy);
        } catch (ClassNotFoundException notInstalled) {
            // Better Mount Steering absent; nothing to drive
        } catch (Throwable t) {
            LOGGER.warn("Failed to register mount camera source with Better Mount Steering: {}", t.toString());
        }
    }

    private static final class SsrCameraSource implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            switch (method.getName()) {
                case "isActive" -> {
                    return ShoulderSurfingHelper.isCameraDecoupled();
                }
                case "yaw" -> {
                    return ShoulderSurfingHelper.getCameraYaw();
                }
                case "xRot" -> {
                    return ShoulderSurfingHelper.getCameraXRot();
                }
                case "onDecoupleEnd" -> {
                    ShoulderSurfingHelper.setLastMovedYRot((float) args[0]);
                    return null;
                }
                case "toString" -> {
                    return "SsrCameraSource";
                }
                case "hashCode" -> {
                    return System.identityHashCode(proxy);
                }
                case "equals" -> {
                    return proxy == args[0];
                }
                default -> {
                    return null;
                }
            }
        }
    }
}
