package com.ssrcamerafixes.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModList;
import org.slf4j.Logger;

import java.lang.reflect.Method;

/**
 * Better Combat compat: query BC's public {@code MinecraftClient_BetterCombat}
 * API to detect "is the player currently in a BC attack" — covers the entire
 * attack lifecycle (upswing + downswing) for any weapon, vanilla or modded,
 * with no hardcoded timing.
 *
 * <p>BC's mixin makes {@code Minecraft.getInstance()} implement
 * {@code net.bettercombat.api.MinecraftClient_BetterCombat}. We invoke its
 * default method {@code isWeaponSwingInProgress()} (defined as
 * {@code getSwingProgress() < 1F}, where {@code swingProgress =
 * lastAttacked / lastSwingDuration}). {@code lastAttacked} increments at
 * BC's {@code pre_Tick} HEAD every tick; {@code lastSwingDuration} is set
 * to {@code attackCooldownTicksFloat} (the weapon's per-attack duration)
 * when an attack starts. This is BC's own source of truth for "attack in
 * progress" — accurate per weapon, no timing assumptions on our end.
 *
 * <p>Reflection-only access — BC is an optional dep, so no class loading
 * occurs unless BC is actually present in {@code ModList}.
 */
public final class BetterCombatHelper {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String BC_MOD_ID = "bettercombat";
    private static final String BC_API_CLASS = "net.bettercombat.api.MinecraftClient_BetterCombat";
    private static final String BC_API_METHOD = "isWeaponSwingInProgress";

    private static final boolean IS_LOADED = ModList.get().isLoaded(BC_MOD_ID);

    private static volatile Method isWeaponSwingInProgressMethod = null;
    private static volatile boolean methodResolved = false;

    private BetterCombatHelper() {}

    private static synchronized void resolveMethod() {
        if (methodResolved) return;
        methodResolved = true;
        if (!IS_LOADED) return;
        try {
            Class<?> apiClass = Class.forName(BC_API_CLASS);
            isWeaponSwingInProgressMethod = apiClass.getMethod(BC_API_METHOD);
            LOGGER.info("Better Combat compat: bound to MinecraftClient_BetterCombat.isWeaponSwingInProgress()");
        } catch (Throwable t) {
            LOGGER.warn("Better Combat compat: failed to bind: {}", t.toString());
        }
    }

    /**
     * @return true while a BC attack is in progress (upswing or downswing) for
     *         any weapon, per BC's own {@code isWeaponSwingInProgress()}. False
     *         when BC is not loaded, when no attack is happening, or when the
     *         API binding failed.
     */
    public static boolean isAttackInProgress() {
        if (!IS_LOADED) return false;
        if (!methodResolved) resolveMethod();
        Method m = isWeaponSwingInProgressMethod;
        if (m == null) return false;
        try {
            Object result = m.invoke(Minecraft.getInstance());
            return result instanceof Boolean && (Boolean) result;
        } catch (Throwable t) {
            return false;
        }
    }
}
