package com.ssrcamerafixes.compat;

import com.github.exopandora.shouldersurfing.api.client.event.SetupCameraRotationEvent;
import com.github.exopandora.shouldersurfing.api.math.Vec2f;
import com.mojang.logging.LogUtils;
import com.ssrcamerafixes.handler.ShoulderCycleHandler;
import net.minecraft.Util;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public final class CombatFocusHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static Method isLockedOn;
    private static Method getTarget;
    private static Method aimPoint;
    private static KeyMapping lockKey;
    private static boolean resolved = false;
    private static long lastNanos;

    private CombatFocusHelper() {}

    private static void resolve() {
        if (resolved) return;
        resolved = true;
        if (!CompatSwitches.enabled("combat_focus")) return;
        try {
            Class<?> handler = Class.forName("tfar.lockon.LockOnHandler");
            isLockedOn = handler.getMethod("isLockedOn");
            getTarget = handler.getMethod("getTarget");
            aimPoint = handler.getMethod("aimPoint", Entity.class);
            lockKey = (KeyMapping) handler.getField("LOCK_ON").get(null);
        } catch (Throwable t) {
            isLockedOn = null;
            LOGGER.debug("Combat Focus lock-on state unavailable: {}", t.toString());
        }
    }

    public static boolean isLockKey(KeyMapping key) {
        resolve();
        return lockKey != null && key != null && lockKey.same(key);
    }

    // Combat Focus zeroes the lock-on pitch when only the camera is above the target's eyes, which the overhead preset always is
    public static void onSetupCameraRotation(SetupCameraRotationEvent event) {
        if (ShoulderCycleHandler.getMode() != ShoulderCycleHandler.Mode.OVERHEAD) return;
        resolve();
        if (isLockedOn == null) return;
        try {
            if (!(Boolean) isLockedOn.invoke(null)) {
                lastNanos = 0L;
                return;
            }
            Entity target = (Entity) getTarget.invoke(null);
            if (target == null) return;
            Vec3 delta = ((Vec3) aimPoint.invoke(null, target)).subtract(Minecraft.getInstance().gameRenderer.getMainCamera().getPosition());
            float wanted = Mth.clamp((float) -Math.toDegrees(Math.atan2(delta.y, delta.horizontalDistance())), -50.0F, 50.0F);
            long now = Util.getNanos();
            float seconds = lastNanos == 0L ? 1.0F : Math.min((now - lastNanos) / 1.0E9F, 0.25F);
            lastNanos = now;
            float from = event.getCameraRotO().x();
            float factor = 1.0F - (float) Math.pow(0.6, seconds * 60.0F);
            event.setResult(new Vec2f(Mth.lerp(factor, from, wanted), event.getResult().y()));
        } catch (Throwable ignored) {
        }
    }
}
