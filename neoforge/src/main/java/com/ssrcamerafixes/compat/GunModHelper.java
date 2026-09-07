package com.ssrcamerafixes.compat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class GunModHelper {

    private GunModHelper() {}

    private static boolean resolved;
    private static Method scorchedGet;
    private static Field scorchedShooting;
    private static Method cgmGet;
    private static Field cgmShooting;

    private static void resolve() {
        if (resolved) return;
        resolved = true;
        try {
            Class<?> c = Class.forName("top.ribs.scguns.client.handler.ShootingHandler");
            scorchedGet = c.getMethod("get");
            scorchedShooting = c.getDeclaredField("shooting");
            scorchedShooting.setAccessible(true);
        } catch (Throwable ignored) {}
        try {
            Class<?> c = Class.forName("com.mrcrayfish.guns.client.handler.ShootingHandler");
            cgmGet = c.getMethod("get");
            cgmShooting = c.getDeclaredField("shooting");
            cgmShooting.setAccessible(true);
        } catch (Throwable ignored) {}
    }

    public static boolean isGunFiring() {
        resolve();
        if (scorchedGet != null && scorchedShooting != null) {
            try {
                if (scorchedShooting.getBoolean(scorchedGet.invoke(null))) return true;
            } catch (Throwable ignored) {}
        }
        if (cgmGet != null && cgmShooting != null) {
            try {
                if (cgmShooting.getBoolean(cgmGet.invoke(null))) return true;
            } catch (Throwable ignored) {}
        }
        return isJegFireLatchActive();
    }

    private static final long FIRE_LATCH_MS = 500L;
    private static long jegFireMs;

    // The JEG port has no shooting flag to poll and its shoot packet carries no rotation, so the server reads the
    // synced yaw when the shot resolves. Latch at the packet and keep re-aiming for a moment after, since mounted
    // Better Mount Steering rewrites the yaw back to the camera angle at tick end
    public static void signalJegFire() {
        jegFireMs = System.currentTimeMillis();
    }

    public static boolean isJegFireLatchActive() {
        return (System.currentTimeMillis() - jegFireMs) < FIRE_LATCH_MS;
    }
}
