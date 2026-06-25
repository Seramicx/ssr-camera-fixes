package com.ssrcamerafixes.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.client.CameraType;
import net.minecraft.world.entity.Entity;
import net.neoforged.fml.ModList;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

// Sable is the physics framework Create Aeronautics rides on. It builds rotating sub-levels with their own
// banked camera and extra CameraType enum entries, which SSR doesn't know about.
public final class SableHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean loaded;
    private static boolean loadedResolved = false;

    private static Method hasCustomEntityOrientation = null;
    private static boolean orientationResolved = false;

    private static CameraType subLevelView = null;
    private static CameraType subLevelViewUnlocked = null;
    private static boolean camerasResolved = false;

    private SableHelper() {}

    public static boolean isLoaded() {
        if (!loadedResolved) {
            loaded = ModList.get().isLoaded("sable");
            loadedResolved = true;
        }
        return loaded;
    }

    private static void resolveOrientation() {
        if (orientationResolved) return;
        orientationResolved = true;
        if (!isLoaded()) return;
        try {
            Class<?> util = Class.forName("dev.ryanhcode.sable.api.entity.EntitySubLevelUtil");
            hasCustomEntityOrientation = util.getMethod("hasCustomEntityOrientation", Entity.class);
        } catch (Throwable t) {
            LOGGER.debug("Sable EntitySubLevelUtil reflection unavailable: {}", t.toString());
        }
    }

    public static boolean isOnSubLevel(Entity entity) {
        if (!isLoaded() || entity == null) return false;
        resolveOrientation();
        if (hasCustomEntityOrientation == null) return false;
        try {
            Object result = hasCustomEntityOrientation.invoke(null, entity);
            return result instanceof Boolean && (Boolean) result;
        } catch (Throwable t) {
            return false;
        }
    }

    private static void resolveCameras() {
        if (camerasResolved) return;
        camerasResolved = true;
        if (!isLoaded()) return;
        try {
            Class<?> types = Class.forName("dev.ryanhcode.sable.mixinhelpers.camera.new_camera_types.SableCameraTypes");
            Field view = types.getField("SUB_LEVEL_VIEW");
            Field unlocked = types.getField("SUB_LEVEL_VIEW_UNLOCKED");
            Object v = view.get(null);
            Object u = unlocked.get(null);
            if (v instanceof CameraType) subLevelView = (CameraType) v;
            if (u instanceof CameraType) subLevelViewUnlocked = (CameraType) u;
        } catch (Throwable t) {
            LOGGER.debug("Sable SableCameraTypes reflection unavailable: {}", t.toString());
        }
    }

    public static boolean isSableCameraType(CameraType cameraType) {
        if (!isLoaded() || cameraType == null) return false;
        resolveCameras();
        return cameraType == subLevelView || cameraType == subLevelViewUnlocked;
    }
}
