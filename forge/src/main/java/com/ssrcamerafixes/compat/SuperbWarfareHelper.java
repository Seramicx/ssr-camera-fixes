package com.ssrcamerafixes.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Field;

public final class SuperbWarfareHelper {

    private static boolean loaded;
    private static boolean loadedResolved = false;

    private static Field zoomField;
    private static Field holdingFireKeyField;
    private static Field zoomPosField;
    private static Field bowPullPosField;
    private static Field cameraLocationField;
    private static Class<?> gunItemClass;
    private static boolean resolved = false;

    private SuperbWarfareHelper() {}

    public static boolean isLoaded() {
        if (!loadedResolved) {
            loaded = ModList.get().isLoaded("superbwarfare");
            loadedResolved = true;
        }
        return loaded;
    }

    private static synchronized void resolve() {
        if (resolved) return;
        resolved = true;
        if (!isLoaded()) return;
        try {
            Class<?> handler = Class.forName("com.atsuishio.superbwarfare.event.ClientEventHandler");
            zoomField = handler.getField("zoom");
            holdingFireKeyField = handler.getField("holdingFireKey");
            zoomPosField = handler.getField("zoomPos");
            bowPullPosField = handler.getField("bowPullPos");
            cameraLocationField = handler.getField("cameraLocation");
            gunItemClass = Class.forName("com.atsuishio.superbwarfare.item.gun.GunItem");
        } catch (Throwable ignored) {}
    }

    public static boolean isZooming() {
        if (!isLoaded()) return false;
        resolve();
        try {
            return zoomField != null && zoomField.getBoolean(null);
        } catch (Throwable ignored) {}
        return false;
    }

    public static boolean isAimingOrFiring() {
        if (!isLoaded()) return false;
        resolve();
        try {
            if (isZooming()) return true;
            if (holdingFireKeyField != null && holdingFireKeyField.getBoolean(null)) return true;
        } catch (Throwable ignored) {}
        return false;
    }

    public static boolean isHoldingGun(Player player) {
        if (player == null || !isLoaded()) return false;
        resolve();
        if (gunItemClass == null) return false;
        try {
            ItemStack stack = player.getMainHandItem();
            Item item = stack != null ? stack.getItem() : null;
            return item != null && gunItemClass.isInstance(item);
        } catch (Throwable ignored) {}
        return false;
    }

    // SW CameraMixin only pulls the 3P camera when max(bowPullPos, zoomPos) > 0.
    public static boolean hasAdsCameraPull() {
        if (!isLoaded()) return false;
        resolve();
        try {
            double zoomPos = zoomPosField != null ? zoomPosField.getDouble(null) : 0.0D;
            double bowPull = bowPullPosField != null ? bowPullPosField.getDouble(null) : 0.0D;
            return Math.max(zoomPos, bowPull) > 0.0D;
        } catch (Throwable ignored) {}
        return false;
    }

    // SW static-inits cameraLocation to 0.6 — lateral ADS bias even with no arrow-key tweak.
    public static void clearCameraLocationBias() {
        if (!isLoaded()) return;
        resolve();
        try {
            if (cameraLocationField != null) {
                cameraLocationField.setDouble(null, 0.0D);
            }
        } catch (Throwable ignored) {}
    }

    private static final long FIRE_LATCH_MS = 500L;
    private static long fireSignalMs = 0L;

    // A semi-auto tap clears holdingFireKey before the server resolves the shot, so latch to hold the aim
    public static void signalFire() {
        fireSignalMs = System.currentTimeMillis();
    }

    public static boolean isFireLatchActive() {
        return (System.currentTimeMillis() - fireSignalMs) < FIRE_LATCH_MS;
    }
}
