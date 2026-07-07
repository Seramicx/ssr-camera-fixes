package com.ssrcamerafixes.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class ConfluenceHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String GUN_PACKAGE = "org.confluence.mod.common.item.gun";
    private static final String MANA_PACKAGE = "org.confluence.mod.common.item.mana";

    private static Class<?> baseGunClass;
    private static boolean baseGunResolved;

    private static boolean loaded;
    private static boolean loadedResolved = false;

    private ConfluenceHelper() {}

    public static boolean isLoaded() {
        if (!loadedResolved) {
            loaded = ModList.get().isLoaded("confluence");
            loadedResolved = true;
        }
        return loaded;
    }

    private static KeyMapping shootKey = null;
    private static KeyMapping aimKey = null;
    private static boolean keysResolved = false;

    private static void resolveKeys() {
        if (keysResolved) return;
        keysResolved = true;
        try {
            Class<?> keys = Class.forName("org.confluence.terra_guns.client.init.TGKeys");
            shootKey = readLazyKey(keys, "SHOOT");
            aimKey = readLazyKey(keys, "AIM");
        } catch (Throwable t) {
            LOGGER.debug("Confluence/TerraGuns key reflection unavailable: {}", t.toString());
        }
    }

    private static KeyMapping readLazyKey(Class<?> keys, String fieldName) {
        try {
            Field field = keys.getField(fieldName);
            Object lazy = field.get(null);
            if (lazy == null) return null;
            Method get = lazy.getClass().getMethod("get");
            Object value = get.invoke(lazy);
            return value instanceof KeyMapping km ? km : null;
        } catch (Throwable t) {
            return null;
        }
    }

    // Confluence's own guns are in GUN_PACKAGE, but TerraGuns registers hand pistol/shotgun/etc as plain BaseGun
    // instances in org.confluence.terra_guns...gun. Both extend BaseGun, so match the base class
    public static boolean isHoldingGun(LocalPlayer player) {
        if (!isLoaded() || player == null) return false;
        Item item = player.getMainHandItem().getItem();
        if (!baseGunResolved) {
            baseGunResolved = true;
            try {
                baseGunClass = Class.forName("org.confluence.terra_guns.common.item.gun.BaseGun");
            } catch (Throwable ignored) {}
        }
        if (baseGunClass != null && baseGunClass.isInstance(item)) return true;
        return item.getClass().getName().startsWith(GUN_PACKAGE);
    }

    public static boolean isHoldingManaWeapon(LocalPlayer player) {
        if (!isLoaded() || player == null) return false;
        Item item = player.getMainHandItem().getItem();
        return item.getClass().getName().startsWith(MANA_PACKAGE);
    }

    public static boolean isGunFiringOrAiming(LocalPlayer player) {
        if (!isHoldingGun(player)) return false;
        if (isFireLatchActive()) return true;
        resolveKeys();
        if (shootKey != null && shootKey.isDown()) return true;
        return aimKey != null && aimKey.isDown();
    }

    private static final long FIRE_LATCH_MS = 500L;
    private static long fireSignalMs = 0L;

    // Manual guns consumeClick() the shoot key the same tick they fire, so isDown misses single shots. Latch at
    // the shoot packet so the crosshair aim holds through the shot even when the key already released
    public static void signalFire() {
        fireSignalMs = System.currentTimeMillis();
    }

    public static boolean isFireLatchActive() {
        return (System.currentTimeMillis() - fireSignalMs) < FIRE_LATCH_MS;
    }
}
