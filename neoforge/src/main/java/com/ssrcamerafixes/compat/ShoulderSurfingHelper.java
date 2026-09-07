package com.ssrcamerafixes.compat;

import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfingCamera;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ShoulderSurfingHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    // v5 exposes getInstance() on the IShoulderSurfing interface; v4 had it on a separate
    // api.client.ShoulderSurfing class. Resolve it reflectively so both versions work.
    private static volatile Method getInstanceMethod;
    private static volatile boolean getInstanceResolved;

    private static volatile Method lookAtCrosshairMethod;
    private static volatile boolean lookAtCrosshairResolved;

    // v5-only; absent on v4's IShoulderSurfing, so free look must not gate on it there
    private static volatile Method lookFollowingCrosshairMethod;
    private static volatile boolean lookFollowingCrosshairResolved;

    private static volatile Field cameraDecoupledField;
    private static volatile boolean cameraDecoupledFieldResolved;
    private static volatile Field lookFollowField;
    private static volatile Field turningLockTimeField;
    private static volatile boolean lookFollowFieldsResolved;

    private static volatile Field lastMovedYRotField;
    private static volatile boolean lastMovedYRotResolved;

    // v5 moved the offset getters onto getClientConfig().getCameraConfig(); v4 had them on getClientConfig()
    private static volatile Method cameraConfigMethod;
    private static volatile boolean offsetPathResolved;

    // Perspective enum moved api.model (v4) -> api.client (v5); resolve reflectively so both work
    private static volatile Method perspectiveCurrent;
    private static volatile Object perspectiveShoulderSurfing;
    private static volatile Object perspectiveThirdPersonBack;
    private static volatile Method changePerspectiveMethod;
    private static volatile boolean perspectiveResolved;

    private ShoulderSurfingHelper() {}

    public static IShoulderSurfing instanceOrNull() {
        return instance();
    }

    private static IShoulderSurfing instance() {
        if (!getInstanceResolved) {
            synchronized (ShoulderSurfingHelper.class) {
                if (!getInstanceResolved) {
                    try {
                        getInstanceMethod = IShoulderSurfing.class.getMethod("getInstance");
                    } catch (NoSuchMethodException e) {
                        try {
                            Class<?> v4 = Class.forName("com.github.exopandora.shouldersurfing.api.client.ShoulderSurfing");
                            getInstanceMethod = v4.getMethod("getInstance");
                        } catch (Throwable t) {
                            LOGGER.debug("SSR getInstance not found on v5 interface or v4 class");
                        }
                    }
                    getInstanceResolved = true;
                }
            }
        }
        Method m = getInstanceMethod;
        if (m == null) return null;
        try {
            return (IShoulderSurfing) m.invoke(null);
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean isShoulderSurfingActive() {
        try {
            IShoulderSurfing ssr = instance();
            return ssr != null && ssr.isShoulderSurfing();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isCameraDecoupled() {
        try {
            IShoulderSurfing ssr = instance();
            return ssr != null && ssr.isShoulderSurfing() && ssr.isCameraDecoupled();
        } catch (Throwable t) {
            return false;
        }
    }

    // Adaptive+isAiming hardcodes isCameraDecoupled=false (no plugin override). Write the field so
    // charged-hold can keep adaptive crosshair (isAiming) without locking walk-to-look.
    public static void forceCameraDecoupled(boolean decoupled) {
        try {
            IShoulderSurfing ssr = instance();
            if (ssr == null) return;
            if (!cameraDecoupledFieldResolved) {
                synchronized (ShoulderSurfingHelper.class) {
                    if (!cameraDecoupledFieldResolved) {
                        try {
                            Field f = ssr.getClass().getDeclaredField("isCameraDecoupled");
                            f.setAccessible(true);
                            cameraDecoupledField = f;
                        } catch (Throwable t) {
                            LOGGER.debug("SSR isCameraDecoupled field not found on {}", ssr.getClass().getName());
                        }
                        cameraDecoupledFieldResolved = true;
                    }
                }
            }
            Field f = cameraDecoupledField;
            if (f != null) f.setBoolean(ssr, decoupled);
        } catch (Throwable ignored) {}
    }

    // v5 pins the body to the crosshair target while aiming, which also cancels its own decoupled
    // movement turn. No such state on v4.
    public static void clearLookFollowingCrosshairTarget() {
        try {
            IShoulderSurfing ssr = instance();
            if (ssr == null) return;
            if (!lookFollowFieldsResolved) {
                synchronized (ShoulderSurfingHelper.class) {
                    if (!lookFollowFieldsResolved) {
                        lookFollowField = declaredField(ssr, "isLookFollowingCrosshairTarget");
                        turningLockTimeField = declaredField(ssr, "turningLockTime");
                        lookFollowFieldsResolved = true;
                    }
                }
            }
            if (lookFollowField != null) lookFollowField.setBoolean(ssr, false);
            if (turningLockTimeField != null) turningLockTimeField.setInt(ssr, 0);
        } catch (Throwable ignored) {}
    }
    private static Field declaredField(Object owner, String name) {
        try {
            Field f = owner.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f;
        } catch (Throwable t) {
            LOGGER.debug("SSR {} field not found on {}", name, owner.getClass().getName());
            return null;
        }
    }
    public static float getCameraYaw() {
        try {
            if (isShoulderSurfingActive()) {
                IShoulderSurfingCamera cam = instance().getCamera();
                if (cam != null) return cam.getYRot();
            }
        } catch (Throwable ignored) {}
        return Minecraft.getInstance().gameRenderer.getMainCamera().getYRot();
    }

    public static float getCameraXRot() {
        try {
            if (isShoulderSurfingActive()) {
                IShoulderSurfingCamera cam = instance().getCamera();
                if (cam != null) return cam.getXRot();
            }
        } catch (Throwable ignored) {}
        return Minecraft.getInstance().gameRenderer.getMainCamera().getXRot();
    }

    public static void setCameraRotation(float yaw, float pitch) {
        try {
            IShoulderSurfing ssr = instance();
            IShoulderSurfingCamera cam = ssr != null ? ssr.getCamera() : null;
            if (cam != null) {
                cam.setYRot(yaw);
                cam.setXRot(pitch);
            }
        } catch (Throwable ignored) {}
    }

    private static void resolvePerspective() {
        if (perspectiveResolved) return;
        synchronized (ShoulderSurfingHelper.class) {
            if (perspectiveResolved) return;
            Class<?> perspective = null;
            for (String name : new String[]{
                    "com.github.exopandora.shouldersurfing.api.client.Perspective",
                    "com.github.exopandora.shouldersurfing.api.model.Perspective"}) {
                try {
                    perspective = Class.forName(name);
                    break;
                } catch (Throwable ignored) {}
            }
            if (perspective != null) {
                try {
                    perspectiveCurrent = perspective.getMethod("current");
                    perspectiveShoulderSurfing = perspective.getField("SHOULDER_SURFING").get(null);
                    perspectiveThirdPersonBack = perspective.getField("THIRD_PERSON_BACK").get(null);
                    changePerspectiveMethod = IShoulderSurfing.class.getMethod("changePerspective", perspective);
                } catch (Throwable e) {
                    LOGGER.debug("SSR Perspective API not resolvable on {}", perspective.getName());
                }
            }
            perspectiveResolved = true;
        }
    }

    public static boolean isShoulderSurfingPerspective() {
        resolvePerspective();
        try {
            return perspectiveCurrent != null && perspectiveCurrent.invoke(null) == perspectiveShoulderSurfing;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isThirdPersonBackPerspective() {
        resolvePerspective();
        try {
            return perspectiveCurrent != null && perspectiveCurrent.invoke(null) == perspectiveThirdPersonBack;
        } catch (Throwable t) {
            return false;
        }
    }

    public static void changeToThirdPersonBack() {
        resolvePerspective();
        try {
            IShoulderSurfing ssr = instance();
            if (ssr != null && changePerspectiveMethod != null) {
                changePerspectiveMethod.invoke(ssr, perspectiveThirdPersonBack);
            }
        } catch (Throwable ignored) {}
    }

    public static void changeToShoulderSurfing() {
        resolvePerspective();
        try {
            IShoulderSurfing ssr = instance();
            if (ssr != null && changePerspectiveMethod != null) {
                changePerspectiveMethod.invoke(ssr, perspectiveShoulderSurfing);
            }
        } catch (Throwable ignored) {}
    }

    public static void swapShoulder() {
        try {
            IShoulderSurfing ssr = instance();
            if (ssr != null) ssr.swapShoulder();
        } catch (Throwable t) {
            LOGGER.warn("ShoulderSurfingHelper.swapShoulder failed: {}", t.toString());
        }
    }

    public static void lookAtCrosshairTarget() {
        try {
            IShoulderSurfing ssr = instance();
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

    public static boolean isLookFollowingCrosshairTarget() {
        try {
            IShoulderSurfing ssr = instance();
            if (ssr == null) return false;
            if (!lookFollowingCrosshairResolved) {
                synchronized (ShoulderSurfingHelper.class) {
                    if (!lookFollowingCrosshairResolved) {
                        try {
                            lookFollowingCrosshairMethod = ssr.getClass().getMethod("isLookFollowingCrosshairTarget");
                        } catch (NoSuchMethodException e) {
                            LOGGER.debug("SSR isLookFollowingCrosshairTarget not found on {}", ssr.getClass().getName());
                        }
                        lookFollowingCrosshairResolved = true;
                    }
                }
            }
            Method m = lookFollowingCrosshairMethod;
            if (m != null) return (boolean) m.invoke(ssr);
        } catch (Throwable ignored) {}
        return false;
    }

    public static double getStoredShoulderX() {
        return configOffset("getOffsetX");
    }

    public static void setLastMovedYRot(float value) {
        IShoulderSurfingCamera cam;
        try {
            IShoulderSurfing ssr = instance();
            cam = ssr != null ? ssr.getCamera() : null;
        } catch (Throwable t) {
            return;
        }
        if (cam == null) return;
        if (!lastMovedYRotResolved) {
            synchronized (ShoulderSurfingHelper.class) {
                if (!lastMovedYRotResolved) {
                    try {
                        Field f = cam.getClass().getDeclaredField("lastMovedYRot");
                        f.setAccessible(true);
                        lastMovedYRotField = f;
                    } catch (NoSuchFieldException e) {
                        LOGGER.debug("SSR lastMovedYRot field not found on {}", cam.getClass().getName());
                    }
                    lastMovedYRotResolved = true;
                }
            }
        }
        Field field = lastMovedYRotField;
        if (field == null) return;
        try {
            field.setFloat(cam, value);
        } catch (Throwable ignored) {}
    }

    private static double configOffset(String getter) {
        try {
            IShoulderSurfing ssr = instance();
            if (ssr == null) return 0.0;
            Object clientConfig = ssr.getClientConfig();
            if (clientConfig == null) return 0.0;
            Object offsetSource = clientConfig;
            if (!offsetPathResolved) {
                synchronized (ShoulderSurfingHelper.class) {
                    if (!offsetPathResolved) {
                        try {
                            cameraConfigMethod = clientConfig.getClass().getMethod("getCameraConfig");
                        } catch (NoSuchMethodException e) {
                            cameraConfigMethod = null;
                        }
                        offsetPathResolved = true;
                    }
                }
            }
            if (cameraConfigMethod != null) {
                offsetSource = cameraConfigMethod.invoke(clientConfig);
            }
            Method m = offsetSource.getClass().getMethod(getter);
            return ((Number) m.invoke(offsetSource)).doubleValue();
        } catch (Throwable t) {
            return 0.0;
        }
    }
}
