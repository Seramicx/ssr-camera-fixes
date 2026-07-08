package com.ssrcamerafixes.compat;

import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfingCamera;
import com.github.exopandora.shouldersurfing.api.client.ShoulderSurfing;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ShoulderSurfingHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static volatile Method lookAtCrosshairMethod;
    private static volatile boolean lookAtCrosshairResolved;

    private static volatile Field lastMovedYRotField;
    private static volatile boolean lastMovedYRotResolved;

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

    public static void setLastMovedYRot(float value) {
        IShoulderSurfingCamera cam;
        try {
            cam = ShoulderSurfing.getInstance().getCamera();
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
