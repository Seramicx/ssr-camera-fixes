package com.ssrcamerafixes.compat;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
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

    private static Method getLocalPlayerPatch;
    private static Method entityStateAttacking;
    private static Method isHoldingAny;
    private static Method getEntityState;
    private static Method entityStateGetState;
    private static Method turningLocked;
    private static Method canUseSkill;
    private static Object updateLivingMotionState;
    private static Method isActionActive;
    private static Object attackAction;
    private static Object attackDestroyAction;
    private static Method cameraApiGetInstance;
    private static Method isLockingOnTarget;
    private static Method getCameraXRot;
    private static Method getCameraXRotO;
    private static Method getCameraYRot;
    private static Method getCameraYRotO;
    private static Method getSkill;
    private static Object moverSlot;
    private static Method skillContainerGetSkill;
    private static boolean resolved = false;

    private EpicFightHelper() {}

    private static void resolve() {
        if (resolved) return;
        resolved = true;
        if (!isLoaded) return;
        try {
            Class<?> caps = Class.forName("yesman.epicfight.world.capabilities.EpicFightCapabilities");
            getLocalPlayerPatch = caps.getMethod("getLocalPlayerPatch", LocalPlayer.class);

            Class<?> patch = Class.forName("yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch");
            getEntityState = patch.getMethod("getEntityState");
            isHoldingAny = patch.getMethod("isHoldingAny");
            getSkill = patch.getMethod("getSkill", Class.forName("yesman.epicfight.skill.SkillSlot"));

            Class<?> entityState = Class.forName("yesman.epicfight.api.animation.types.EntityState");
            entityStateAttacking = entityState.getMethod("attacking");
            entityStateGetState = entityState.getMethod("getState", Class.forName("yesman.epicfight.api.animation.types.EntityState$StateFactor"));
            turningLocked = entityState.getMethod("turningLocked");
            canUseSkill = entityState.getMethod("canUseSkill");
            updateLivingMotionState = entityState.getField("UPDATE_LIVING_MOTION").get(null);

            Class<?> inputManager = Class.forName("yesman.epicfight.api.client.input.InputManager");
            Class<?> inputAction = Class.forName("yesman.epicfight.api.client.input.action.InputAction");
            isActionActive = inputManager.getMethod("isActionActive", inputAction);
            attackAction = Class.forName("yesman.epicfight.api.client.input.action.EpicFightInputAction").getField("ATTACK").get(null);
            attackDestroyAction = Class.forName("yesman.epicfight.api.client.input.action.MinecraftInputAction").getField("ATTACK_DESTROY").get(null);

            Class<?> cameraApi = Class.forName("yesman.epicfight.api.client.camera.EpicFightCameraAPI");
            cameraApiGetInstance = cameraApi.getMethod("getInstance");
            isLockingOnTarget = cameraApi.getMethod("isLockingOnTarget");
            getCameraXRot = cameraApi.getMethod("getCameraXRot");
            getCameraXRotO = cameraApi.getMethod("getCameraXRotO");
            getCameraYRot = cameraApi.getMethod("getCameraYRot");
            getCameraYRotO = cameraApi.getMethod("getCameraYRotO");

            Class<?> skillSlots = Class.forName("yesman.epicfight.skill.SkillSlots");
            moverSlot = skillSlots.getField("MOVER").get(null);
            Class<?> skillContainer = Class.forName("yesman.epicfight.skill.SkillContainer");
            skillContainerGetSkill = skillContainer.getMethod("getSkill");
        } catch (Throwable ignored) {
            getLocalPlayerPatch = null;
        }
    }

    private static Object localPatch(LocalPlayer player) {
        resolve();
        if (getLocalPlayerPatch == null || player == null) return null;
        try {
            return getLocalPlayerPatch.invoke(null, player);
        } catch (Throwable t) {
            return null;
        }
    }

    public static void signalAttack() {
        attackSignalMsO = System.currentTimeMillis();
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

    public static boolean isHoldingSkill(LocalPlayer player) {
        if (!isLoaded || player == null) return false;
        Object patch = localPatch(player);
        if (patch == null || isHoldingAny == null) return false;
        try {
            return (boolean) isHoldingAny.invoke(patch);
        } catch (Throwable t) {
            return false;
        }
    }

    // Mirrors EF ShoulderSurfingCompat.CameraCouplingOnAttack on SSR v4.
    public static boolean isAttackKeyActive() {
        if (!isLoaded) return false;
        resolve();
        if (isActionActive == null || attackAction == null || attackDestroyAction == null) return false;
        try {
            return (boolean) isActionActive.invoke(null, attackAction)
                    || (boolean) isActionActive.invoke(null, attackDestroyAction);
        } catch (Throwable t) {
            return false;
        }
    }

    // Mirrors EF ShoulderSurfingCompat: attack key held OR any holdable/chargeable skill.
    public static boolean needsCameraCoupling(LocalPlayer player) {
        return isAttackKeyActive() || isHoldingSkill(player);
    }

    public static boolean isLockOnTargeting() {
        if (!isLoaded) return false;
        resolve();
        if (cameraApiGetInstance == null || isLockingOnTarget == null) return false;
        try {
            Object api = cameraApiGetInstance.invoke(null);
            return api != null && (boolean) isLockingOnTarget.invoke(api);
        } catch (Throwable t) {
            return false;
        }
    }

    public static float getLockOnXRot(float partialTick) {
        if (!isLoaded) return 0;
        resolve();
        if (cameraApiGetInstance == null || getCameraXRot == null || getCameraXRotO == null) return 0;
        try {
            Object api = cameraApiGetInstance.invoke(null);
            if (api == null) return 0;
            float cur = ((Number) getCameraXRot.invoke(api)).floatValue();
            float prev = ((Number) getCameraXRotO.invoke(api)).floatValue();
            return Mth.rotLerp(partialTick, prev, cur);
        } catch (Throwable t) {
            return 0;
        }
    }

    public static float getLockOnYRot(float partialTick) {
        if (!isLoaded) return 0;
        resolve();
        if (cameraApiGetInstance == null || getCameraYRot == null || getCameraYRotO == null) return 0;
        try {
            Object api = cameraApiGetInstance.invoke(null);
            if (api == null) return 0;
            float cur = ((Number) getCameraYRot.invoke(api)).floatValue();
            float prev = ((Number) getCameraYRotO.invoke(api)).floatValue();
            return Mth.rotLerp(partialTick, prev, cur);
        } catch (Throwable t) {
            return 0;
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
        if (player.onGround()) return false;
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

    // True while WoM's Aqua Maneuver mover skill is the equipped mover and the player is sprint-swimming.
    public static boolean isWomMoverSwimming(LocalPlayer player) {
        if (!isLoaded || player == null) return false;
        if (!player.isInWater() || !player.isSprinting()) return false;
        Object patch = localPatch(player);
        if (patch == null || getSkill == null || moverSlot == null || skillContainerGetSkill == null) return false;
        try {
            Object mover = getSkill.invoke(patch, moverSlot);
            if (mover == null) return false;
            Object skill = skillContainerGetSkill.invoke(mover);
            return skill != null && skill.getClass().getName().startsWith("reascer.wom");
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
