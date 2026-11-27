package com.ssrcamerafixes.compat;

import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.client.Perspective;
import com.github.exopandora.shouldersurfing.api.config.IPerspectiveConfig;
import com.mojang.logging.LogUtils;
import com.ssrcamerafixes.mixin.AccessorOptionsCameraType;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Method;

public final class SableCameraHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static Object companion;
    private static Method getVehicleSubLevel;
    private static Method preCycle;
    private static Method postCycle;
    private static CameraType subLevelView;
    private static CameraType subLevelViewUnlocked;
    private static boolean resolved = false;

    private SableCameraHelper() {}

    private static void resolve() {
        if (resolved) return;
        resolved = true;
        if (!CompatSwitches.enabled("sable")) return;
        try {
            Class<?> types = Class.forName("dev.ryanhcode.sable.mixinhelpers.camera.new_camera_types.SableCameraTypes");
            subLevelView = (CameraType) types.getField("SUB_LEVEL_VIEW").get(null);
            subLevelViewUnlocked = (CameraType) types.getField("SUB_LEVEL_VIEW_UNLOCKED").get(null);
            companion = Class.forName("dev.ryanhcode.sable.Sable").getField("HELPER").get(null);
            getVehicleSubLevel = companion.getClass().getMethod("getVehicleSubLevel", Entity.class);
            // Sable's F5 hooks are mixin handlers merged into Minecraft, and Mixin may prefix their names
            for (Method m : Minecraft.class.getDeclaredMethods()) {
                if (m.getParameterCount() != 1 || m.getParameterTypes()[0] != CallbackInfo.class) continue;
                if (m.getName().endsWith("sable$preCycleCameraType")) preCycle = m;
                if (m.getName().endsWith("sable$postCycleCameraType")) postCycle = m;
            }
            if (preCycle != null) preCycle.setAccessible(true);
            if (postCycle != null) postCycle.setAccessible(true);
        } catch (Throwable t) {
            subLevelView = null;
            subLevelViewUnlocked = null;
            getVehicleSubLevel = null;
            LOGGER.debug("Sable camera types unavailable: {}", t.toString());
        }
    }

    public static boolean isSableView(@Nullable CameraType type) {
        resolve();
        return type != null && (type == subLevelView || type == subLevelViewUnlocked);
    }

    public static boolean isRidingSubLevel(@Nullable Entity entity) {
        resolve();
        if (entity == null || getVehicleSubLevel == null) return false;
        try {
            return getVehicleSubLevel.invoke(companion, entity) != null;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean handlesF5(Minecraft mc) {
        if (!SableGuard.perspectiveGuard) return false;
        return isSableView(mc.options.getCameraType()) || isRidingSubLevel(mc.getCameraEntity());
    }

    // Shoulder Surfing's own cycle with Sable's two views inserted after shoulder surfing
    public static void cycle(Minecraft mc) {
        IShoulderSurfing ssr = IShoulderSurfing.getInstance();
        IPerspectiveConfig config;
        try {
            config = ssr.getClientConfig().getPerspectiveConfig();
        } catch (Throwable t) {
            ssr.changePerspective(Perspective.SHOULDER_SURFING);
            return;
        }
        CameraType current = mc.options.getCameraType();
        boolean seated = isRidingSubLevel(mc.getCameraEntity());

        hook(mc, preCycle);
        if (seated && current == subLevelView) {
            setSableView(mc, ssr, subLevelViewUnlocked);
        } else if (isSableView(current)) {
            ssr.changePerspective(Perspective.SHOULDER_SURFING.next(config));
        } else if (seated && Perspective.current() == Perspective.SHOULDER_SURFING) {
            setSableView(mc, ssr, subLevelView);
        } else {
            ssr.changePerspective(Perspective.current().next(config));
        }
    }

    public static void leaveSableView(Minecraft mc) {
        hook(mc, preCycle);
        IShoulderSurfing.getInstance().changePerspective(Perspective.SHOULDER_SURFING);
    }

    private static void setSableView(Minecraft mc, IShoulderSurfing ssr, CameraType type) {
        if (ssr.isShoulderSurfing()) ssr.changePerspective(Perspective.THIRD_PERSON_BACK);
        ((AccessorOptionsCameraType) mc.options).ssrcamerafixes$setCameraTypeField(type);
        hook(mc, postCycle);
    }

    private static void hook(Minecraft mc, @Nullable Method method) {
        if (method == null) return;
        try {
            method.invoke(mc, new CallbackInfo("handleKeybinds", false));
        } catch (Throwable t) {
            LOGGER.debug("Sable camera hook failed: {}", t.toString());
        }
    }
}
