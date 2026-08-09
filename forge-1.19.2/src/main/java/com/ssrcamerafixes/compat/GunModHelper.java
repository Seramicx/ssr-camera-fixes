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
    private static Method cgmShootKeyGet;
    private static Method cgmAimingGet;
    private static Method cgmIsAiming;
    private static Class<?> cgmGunItemClass;

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
        try {
            Class<?> keyBinds = Class.forName("com.mrcrayfish.guns.client.KeyBinds");
            cgmShootKeyGet = keyBinds.getMethod("getShootMapping");
        } catch (Throwable ignored) {}
        try {
            Class<?> aiming = Class.forName("com.mrcrayfish.guns.client.handler.AimingHandler");
            cgmAimingGet = aiming.getMethod("get");
            cgmIsAiming = aiming.getMethod("isAiming");
        } catch (Throwable ignored) {}
        try {
            cgmGunItemClass = Class.forName("com.mrcrayfish.guns.item.GunItem");
        } catch (Throwable ignored) {}
    }

    public static boolean isHoldingGun(net.minecraft.world.entity.player.Player player) {
        resolve();
        if (cgmGunItemClass == null) return false;
        return cgmGunItemClass.isInstance(player.getMainHandItem().getItem());
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
        return false;
    }

    public static boolean isShootKeyDown() {
        resolve();
        if (cgmShootKeyGet == null) return false;
        try {
            Object mapping = cgmShootKeyGet.invoke(null);
            if (mapping == null) return false;
            Object down = mapping.getClass().getMethod("isDown").invoke(mapping);
            return down instanceof Boolean && (Boolean) down;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isShootKeyDownOrFiring() {
        return isShootKeyDown() || isGunFiring();
    }

    public static boolean isAiming() {
        resolve();
        if (cgmAimingGet == null || cgmIsAiming == null) return false;
        try {
            Object handler = cgmAimingGet.invoke(null);
            Object aiming = cgmIsAiming.invoke(handler);
            return aiming instanceof Boolean && (Boolean) aiming;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
