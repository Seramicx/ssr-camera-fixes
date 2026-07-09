package com.ssrcamerafixes.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class EpicFightHelper {

    public static final EpicFightHelper INSTANCE = new EpicFightHelper();

    private static final long CAST_LATCH_MS = 500L;
    private static long castSignalMsO = 0L;

    private static final long ATTACK_LATCH_MS = 1000L;
    private static long attackSignalMsO = 0L;

    private static final boolean isLoaded = ModList.get().isLoaded("epicfight");

    private static Method getEntityPatch;
    private static Class<?> localPlayerPatchClass;
    private static Method entityStateAttacking;
    private static Method getEntityState;
    private static Method entityStateGetState;
    private static Method turningLocked;
    private static Method canUseSkill;
    private static Method inaction;
    private static Object updateLivingMotionState;
    private static Method isTargetLockedOn;
    private static Method isBattleMode;
    private static Method isChargingSkill;
    private static Field modelYRotField;
    private static boolean modelYRotResolved = false;
    private static boolean resolved = false;

    private EpicFightHelper() {}

    private static void resolve() {
        if (resolved) return;
        resolved = true;
        if (!isLoaded) return;
        try {
            localPlayerPatchClass = Class.forName(
                    "yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch");
            Class<?> caps = Class.forName("yesman.epicfight.world.capabilities.EpicFightCapabilities");
            getEntityPatch = caps.getMethod("getEntityPatch",
                    net.minecraft.world.entity.Entity.class, Class.class);

            getEntityState = localPlayerPatchClass.getMethod("getEntityState");
            isTargetLockedOn = localPlayerPatchClass.getMethod("isTargetLockedOn");
            isBattleMode = localPlayerPatchClass.getMethod("isBattleMode");
            isChargingSkill = localPlayerPatchClass.getMethod("isChargingSkill");

            Class<?> entityState = Class.forName("yesman.epicfight.api.animation.types.EntityState");
            entityStateAttacking = entityState.getMethod("attacking");
            entityStateGetState = entityState.getMethod("getState",
                    Class.forName("yesman.epicfight.api.animation.types.EntityState$StateFactor"));
            turningLocked = entityState.getMethod("turningLocked");
            canUseSkill = entityState.getMethod("canUseSkill");
            inaction = entityState.getMethod("inaction");
            updateLivingMotionState = entityState.getField("UPDATE_LIVING_MOTION").get(null);
        } catch (Throwable ignored) {
            getEntityPatch = null;
        }
    }

    private static Object localPatch(LocalPlayer player) {
        resolve();
        if (getEntityPatch == null || localPlayerPatchClass == null || player == null) return null;
        try {
            return getEntityPatch.invoke(null, player, localPlayerPatchClass);
        } catch (Throwable t) {
            return null;
        }
    }

    public static void signalAttack() {
        attackSignalMsO = System.currentTimeMillis();
    }

    // Instant casts (Firebolt) finish within a tick, so latch on the press edge to hold the aim-face
    public static void signalCast() {
        castSignalMsO = System.currentTimeMillis();
    }

    public static boolean isAttacking(LocalPlayer player) {
        if (!isLoaded || player == null) return false;
        if ((System.currentTimeMillis() - attackSignalMsO) < ATTACK_LATCH_MS) return true;
        Object patch = localPatch(player);
        if (patch == null || getEntityState == null || entityStateAttacking == null) return false;
        try {
            Object state = getEntityState.invoke(patch);
            return state != null && (boolean) entityStateAttacking.invoke(state);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isLockOnTargeting() {
        if (!isLoaded) return false;
        LocalPlayer player = Minecraft.getInstance().player;
        Object patch = localPatch(player);
        if (patch == null || isTargetLockedOn == null) return false;
        try {
            return (boolean) isTargetLockedOn.invoke(patch);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean animationOwnsLivingMotion(LocalPlayer player) {
        if (!isLoaded || player == null) return false;
        Object patch = localPatch(player);
        if (patch == null || getEntityState == null || entityStateGetState == null || updateLivingMotionState == null) {
            return false;
        }
        try {
            Object state = getEntityState.invoke(patch);
            if (state == null) return false;
            return !(boolean) entityStateGetState.invoke(state, updateLivingMotionState);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isWallClimbing(LocalPlayer player) {
        if (!isLoaded || player == null) return false;
        if (player.isOnGround()) return false;
        Object patch = localPatch(player);
        if (patch == null || getEntityState == null || entityStateGetState == null || updateLivingMotionState == null
                || turningLocked == null || canUseSkill == null) {
            return false;
        }
        try {
            Object state = getEntityState.invoke(patch);
            if (state == null) return false;
            boolean updateLivingMotion = (boolean) entityStateGetState.invoke(state, updateLivingMotionState);
            if (updateLivingMotion) return false;
            if ((boolean) turningLocked.invoke(state)) return false;
            if ((boolean) canUseSkill.invoke(state)) return false;
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static Field resolveModelYRotField(Object patch) {
        if (!modelYRotResolved) {
            modelYRotResolved = true;
            Class<?> c = patch.getClass();
            while (c != null && modelYRotField == null) {
                try {
                    modelYRotField = c.getDeclaredField("modelYRot");
                    modelYRotField.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    c = c.getSuperclass();
                }
            }
        }
        return modelYRotField;
    }

    public static float getModelYRot(LocalPlayer player) {
        if (!isLoaded || player == null) return Float.NaN;
        Object patch = localPatch(player);
        if (patch == null) return Float.NaN;
        try {
            Field f = resolveModelYRotField(patch);
            if (f == null) return Float.NaN;
            return f.getFloat(patch);
        } catch (Throwable t) {
            return Float.NaN;
        }
    }

    public static void setModelYRot(LocalPlayer player, float value) {
        if (!isLoaded || player == null) return;
        Object patch = localPatch(player);
        if (patch == null) return;
        try {
            Field f = resolveModelYRotField(patch);
            if (f != null) f.setFloat(patch, value);
        } catch (Throwable ignored) {}
    }

    // Mirrors PhantomAscentSkill's own trigger guard so the camera-facing snap only fires in the same window
    public static boolean isAirborneSkillContext(LocalPlayer player) {
        if (!isLoaded || player == null) return false;
        if (player.isOnGround() || player.getVehicle() != null || player.getAbilities().flying) return false;
        Object patch = localPatch(player);
        if (patch == null || isBattleMode == null || isChargingSkill == null
                || getEntityState == null || inaction == null) {
            return false;
        }
        try {
            if (!(boolean) isBattleMode.invoke(patch)) return false;
            if ((boolean) isChargingSkill.invoke(patch)) return false;
            Object state = getEntityState.invoke(patch);
            return state != null && !(boolean) inaction.invoke(state);
        } catch (Throwable t) {
            return false;
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!IronSpellsHelper.isLoaded()) return;

        if (IronSpellsHelper.anyCastKeymapDown() || IronSpellsHelper.isCasting()) {
            castSignalMsO = System.currentTimeMillis();
        }
    }

    private static boolean castLatchActive() {
        return (System.currentTimeMillis() - castSignalMsO) < CAST_LATCH_MS;
    }

    public static boolean isCastLatchActive() {
        return castLatchActive();
    }

    public static boolean isAiming(LocalPlayer player) {
        if (player == null) return false;

        if (player.isUsingItem()) {
            ItemStack stack = player.getUseItem();
            if (stack.getItem() instanceof BowItem
                    || stack.getItem() instanceof CrossbowItem
                    || stack.getItem() instanceof TridentItem) {
                return true;
            }
            if (IronSpellsHelper.isIronsItem(stack.getItem())) return true;
        }

        return castLatchActive()
                || IronSpellsHelper.isCasting()
                || IronSpellsHelper.anyCastKeymapDown();
    }
}
