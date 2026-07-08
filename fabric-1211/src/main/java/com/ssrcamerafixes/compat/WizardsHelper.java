package com.ssrcamerafixes.compat;

import com.mojang.logging.LogUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public final class WizardsHelper {

    public static final WizardsHelper INSTANCE = new WizardsHelper();

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long CAST_LATCH_MS = 500L;

    private static Boolean spellEnginePresent;
    private static Method isCastingSpellMethod;
    private static Method getSpellCastProgressMethod;
    private static Method getCurrentSkillAttackMethod;
    private static boolean castMethodResolved;

    private static Field hotbarInstanceField;
    private static Field hotbarSlotsField;
    private static Method slotGetKeyBindingMethod;
    private static boolean hotbarResolved;

    private static long castSignalMsO;

    private WizardsHelper() {}

    public static boolean isLoaded() {
        if (spellEnginePresent == null) {
            spellEnginePresent = FabricLoader.getInstance().isModLoaded("spell_engine");
        }
        return spellEnginePresent;
    }

    private static synchronized void resolveCastMethod() {
        if (castMethodResolved) return;
        castMethodResolved = true;
        if (!isLoaded()) return;
        try {
            Class<?> iface = Class.forName("net.spell_engine.internals.casting.SpellCasterClient");
            isCastingSpellMethod = iface.getMethod("isCastingSpell");
            getSpellCastProgressMethod = iface.getMethod("getSpellCastProgress");
            // getCurrentSkillAttack: 1.21.1+ only
            try {
                getCurrentSkillAttackMethod = iface.getMethod("getCurrentSkillAttack");
            } catch (NoSuchMethodException ignored) {
                getCurrentSkillAttackMethod = null;
            }
        } catch (Throwable t) {
            LOGGER.debug("WizardsHelper: spell cast reflection unavailable: {}", t.toString());
        }
    }

    private static synchronized void resolveHotbar() {
        if (hotbarResolved) return;
        hotbarResolved = true;
        if (!isLoaded()) return;
        try {
            Class<?> hotbar = Class.forName("net.spell_engine.client.input.SpellHotbar");
            hotbarInstanceField = hotbar.getField("INSTANCE");
            hotbarSlotsField = hotbar.getField("slots");
            Class<?> slot = Class.forName("net.spell_engine.client.input.SpellHotbar$Slot");
            slotGetKeyBindingMethod = slot.getMethod("getKeyBinding", net.minecraft.client.Options.class);
        } catch (Throwable t) {
            LOGGER.debug("WizardsHelper: SpellHotbar reflection unavailable: {}", t.toString());
        }
    }

    private static boolean isCastKeyDown() {
        resolveHotbar();
        if (hotbarInstanceField == null || hotbarSlotsField == null || slotGetKeyBindingMethod == null) return false;
        try {
            Object instance = hotbarInstanceField.get(null);
            if (instance == null) return false;
            Object slots = hotbarSlotsField.get(instance);
            if (!(slots instanceof List<?> list)) return false;
            net.minecraft.client.Options options = Minecraft.getInstance().options;
            for (Object slot : list) {
                Object km = slotGetKeyBindingMethod.invoke(slot, options);
                if (km instanceof KeyMapping mapping && mapping.isDown()) return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public static boolean isCastingLive() {
        if (!isLoaded()) return false;
        resolveCastMethod();
        if (isCastingSpellMethod == null) return false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;
        try {
            Object v = isCastingSpellMethod.invoke(player);
            return v instanceof Boolean && (Boolean) v;
        } catch (Throwable ignored) {
            return false;
        }
    }

    public static boolean isInstantCasting() {
        if (!isCastingLive()) return false;
        Integer length = currentCastLength();
        return length != null && length == 0;
    }

    private static Integer currentCastLength() {
        resolveCastMethod();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return null;
        Integer fromProgress = lengthFromProgress(player);
        if (fromProgress != null) return fromProgress;
        return lengthFromProcess(player);
    }

    private static Integer lengthFromProgress(LocalPlayer player) {
        if (getSpellCastProgressMethod == null) return null;
        try {
            Object progress = getSpellCastProgressMethod.invoke(player);
            if (progress == null) return null;
            Object process = progress.getClass().getMethod("process").invoke(progress);
            if (process == null) return null;
            Object length = process.getClass().getMethod("length").invoke(process);
            return length instanceof Integer ? (Integer) length : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Integer lengthFromProcess(LocalPlayer player) {
        try {
            Object process = player.getClass().getMethod("getSpellCastProcess").invoke(player);
            if (process == null) return null;
            Object length = process.getClass().getMethod("length").invoke(process);
            return length instanceof Integer ? (Integer) length : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    public void tickLatch() {
        if (!isLoaded()) return;
        if (isCastingLive() || isCastKeyDown()) {
            castSignalMsO = System.currentTimeMillis();
        }
    }

    public static boolean isCasting() {
        if (!isLoaded()) return false;
        if (isCastingLive()) return true;
        if (isCastKeyDown()) return true;
        return (System.currentTimeMillis() - castSignalMsO) < CAST_LATCH_MS;
    }

    public static boolean isMeleeSkillActive() {
        if (!isLoaded()) return false;
        resolveCastMethod();
        if (getCurrentSkillAttackMethod == null) return false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return false;
        try {
            return getCurrentSkillAttackMethod.invoke(player) != null;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
