package com.ssrcamerafixes.compat;

import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

public final class JegHelper {

    private static final long FIRE_LATCH_MS = 500L;
    private static long fireMs;

    private static Method telescopicSightMethod;
    private static boolean telescopicSightResolved;

    private JegHelper() {}

    // The 1.21.1 port has no shooting flag to poll and its shoot packet carries no rotation, so the server reads the
    // synced yaw when the shot resolves. Latch at the packet and keep re-aiming for a moment after, since mounted
    // Better Mount Steering rewrites the yaw back to the camera angle at tick end
    public static void signalFire() {
        fireMs = System.currentTimeMillis();
    }

    public static boolean isFireLatchActive() {
        return (System.currentTimeMillis() - fireMs) < FIRE_LATCH_MS;
    }

    public static boolean hasTelescopicSight(ItemStack stack) {
        if (!telescopicSightResolved) {
            synchronized (JegHelper.class) {
                if (!telescopicSightResolved) {
                    try {
                        telescopicSightMethod = Class.forName("ttv.migami.jeg.gun.GunScopeSupport")
                                .getMethod("hasTelescopicSight", ItemStack.class);
                    } catch (Throwable ignored) {}
                    telescopicSightResolved = true;
                }
            }
        }
        Method m = telescopicSightMethod;
        if (m == null) return false;
        try {
            return (boolean) m.invoke(null, stack);
        } catch (Throwable t) {
            return false;
        }
    }

    private static int shotDelay;
    private static boolean resending;
    private static Object deferredHand;

    public static boolean isResending() {
        return resending;
    }

    public static void deferShot(Object hand) {
        deferredHand = hand;
        shotDelay = 2;
    }

    public static void flushDeferredShot() {
        if (shotDelay <= 0 || --shotDelay > 0) return;
        resending = true;
        try {
            for (java.lang.reflect.Method m : Class.forName("ttv.migami.jeg.network.ClientNetworkHandler").getMethods()) {
                if (m.getName().equals("sendShoot") && m.getParameterCount() == 1) {
                    m.invoke(null, deferredHand);
                    break;
                }
            }
        } catch (Throwable ignored) {
        } finally {
            resending = false;
        }
    }
}
