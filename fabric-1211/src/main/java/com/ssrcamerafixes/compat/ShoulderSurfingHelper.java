package com.ssrcamerafixes.compat;

import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfingCamera;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
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

    private static volatile Field lastMovedYRotField;
    private static volatile boolean lastMovedYRotResolved;

    // v5 moved the offset getters onto getClientConfig().getCameraConfig(); v4 had them on getClientConfig()
    private static volatile Method cameraConfigMethod;
    private static volatile boolean offsetPathResolved;

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

    public static void snapAimToCamera(LocalPlayer player, boolean alignMount, boolean sendRotPacket) {
        if (!isShoulderSurfingActive()) return;
        float camYaw = Mth.wrapDegrees(getCameraYaw());
        float camXRot = getCameraXRot();
        player.setYRot(camYaw);
        player.yRotO = camYaw;
        player.setXRot(camXRot);
        player.xRotO = camXRot;
        player.yBodyRot = camYaw;
        player.yBodyRotO = camYaw;
        player.yHeadRot = camYaw;
        player.yHeadRotO = camYaw;

        if (alignMount) {
            Entity vehicle = player.getVehicle();
            if (vehicle instanceof Mob mount) {
                mount.setYRot(camYaw);
                mount.yRotO = camYaw;
                mount.yBodyRot = camYaw;
                mount.yBodyRotO = camYaw;
            }
        }

        if (sendRotPacket) {
            ClientPacketListener conn = Minecraft.getInstance().getConnection();
            if (conn != null) {
                conn.send(new ServerboundMovePlayerPacket.Rot(camYaw, camXRot, player.onGround()));
            }
        }
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
