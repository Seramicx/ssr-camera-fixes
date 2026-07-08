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

public final class EpicFightHelper {

    public static final EpicFightHelper INSTANCE = new EpicFightHelper();

    private static final long CAST_LATCH_MS = 500L;
    private static long castSignalMsO = 0L;

    private static final long ATTACK_LATCH_MS = 1000L;
    private static long attackSignalMsO = 0L;

    private EpicFightHelper() {}

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
        try {
            yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch patch
                    = yesman.epicfight.world.capabilities.EpicFightCapabilities.getEntityPatch(player,
                            yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch.class);
            if (patch == null) return false;
            return patch.getEntityState().attacking();
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean isLoaded = ModList.get().isLoaded("epicfight");

    public static boolean isLockOnTargeting() {
        if (!isLoaded) return false;
        try {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) return false;
            yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch patch
                    = yesman.epicfight.world.capabilities.EpicFightCapabilities.getEntityPatch(player,
                            yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch.class);
            if (patch == null) return false;
            return patch.isTargetLockedOn();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean animationOwnsLivingMotion(LocalPlayer player) {
        if (!isLoaded || player == null) return false;
        try {
            yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch patch
                    = yesman.epicfight.world.capabilities.EpicFightCapabilities.getEntityPatch(player,
                            yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch.class);
            if (patch == null) return false;
            return !patch.getEntityState().getState(
                    yesman.epicfight.api.animation.types.EntityState.UPDATE_LIVING_MOTION);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isWallClimbing(LocalPlayer player) {
        if (!isLoaded || player == null) return false;
        if (player.isOnGround()) return false;
        try {
            yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch patch
                    = yesman.epicfight.world.capabilities.EpicFightCapabilities.getEntityPatch(player,
                            yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch.class);
            if (patch == null) return false;
            yesman.epicfight.api.animation.types.EntityState state = patch.getEntityState();
            boolean updateLivingMotion = state.getState(
                    yesman.epicfight.api.animation.types.EntityState.UPDATE_LIVING_MOTION);
            if (updateLivingMotion) return false;
            if (state.turningLocked()) return false;
            if (state.canUseSkill()) return false;
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    private static java.lang.reflect.Field modelYRotField = null;
    private static boolean modelYRotResolved = false;

    private static java.lang.reflect.Field resolveModelYRotField(Object patch) {
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
        try {
            yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch patch
                    = yesman.epicfight.world.capabilities.EpicFightCapabilities.getEntityPatch(player,
                            yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch.class);
            if (patch == null) return Float.NaN;
            java.lang.reflect.Field f = resolveModelYRotField(patch);
            if (f == null) return Float.NaN;
            return f.getFloat(patch);
        } catch (Throwable t) {
            return Float.NaN;
        }
    }

    public static void setModelYRot(LocalPlayer player, float value) {
        if (!isLoaded || player == null) return;
        try {
            yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch patch
                    = yesman.epicfight.world.capabilities.EpicFightCapabilities.getEntityPatch(player,
                            yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch.class);
            if (patch == null) return;
            java.lang.reflect.Field f = resolveModelYRotField(patch);
            if (f != null) f.setFloat(patch, value);
        } catch (Throwable ignored) {}
    }

    // Mirrors PhantomAscentSkill's own trigger guard so the camera-facing snap only fires in the same window
    public static boolean isAirborneSkillContext(LocalPlayer player) {
        if (!isLoaded || player == null) return false;
        if (player.isOnGround() || player.getVehicle() != null || player.getAbilities().flying) return false;
        try {
            yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch patch
                    = yesman.epicfight.world.capabilities.EpicFightCapabilities.getEntityPatch(player,
                            yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch.class);
            if (patch == null) return false;
            if (!patch.isBattleMode()) return false;
            if (patch.isChargingSkill()) return false;
            return !patch.getEntityState().inaction();
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
