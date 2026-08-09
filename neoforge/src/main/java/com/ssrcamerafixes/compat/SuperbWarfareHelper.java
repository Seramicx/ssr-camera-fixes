package com.ssrcamerafixes.compat;

import net.neoforged.fml.ModList;

import java.lang.reflect.Field;

public final class SuperbWarfareHelper {

    private static boolean loaded;
    private static boolean loadedResolved = false;

    private static Field zoomField;
    private static Field holdingFireKeyField;
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
        } catch (Throwable ignored) {}
    }

    public static boolean isAimingOrFiring() {
        if (!isLoaded()) return false;
        resolve();
        try {
            if (zoomField != null && zoomField.getBoolean(null)) return true;
            if (holdingFireKeyField != null && holdingFireKeyField.getBoolean(null)) return true;
        } catch (Throwable ignored) {}
        return false;
    }
}
