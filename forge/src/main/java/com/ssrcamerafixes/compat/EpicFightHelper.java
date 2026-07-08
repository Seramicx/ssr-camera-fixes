package com.ssrcamerafixes.compat;

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

    public static boolean isAttacking(LocalPlayer player) {
        if (!isLoaded || player == null) return false;
        if ((System.currentTimeMillis() - attackSignalMsO) < ATTACK_LATCH_MS) return true;
        try {
            yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch patch
                    = yesman.epicfight.world.capabilities.EpicFightCapabilities.getLocalPlayerPatch(player);
            if (patch == null) return false;
            return patch.getEntityState().attacking();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isHoldingSkill(LocalPlayer player) {
        if (!isLoaded || player == null) return false;
        try {
            yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch patch
                    = yesman.epicfight.world.capabilities.EpicFightCapabilities.getLocalPlayerPatch(player);
            return patch != null && patch.isHoldingAny();
        } catch (Throwable t) {
            return false;
        }
    }

    // Mirrors EF ShoulderSurfingCompat.CameraCouplingOnAttack on SSR v4.
    public static boolean isAttackKeyActive() {
        if (!isLoaded) return false;
        try {
            return yesman.epicfight.api.client.input.InputManager.isActionActive(
                            yesman.epicfight.api.client.input.action.EpicFightInputAction.ATTACK)
                    || yesman.epicfight.api.client.input.InputManager.isActionActive(
                            yesman.epicfight.api.client.input.action.MinecraftInputAction.ATTACK_DESTROY);
        } catch (Throwable t) {
            return false;
        }
    }

    // Mirrors EF ShoulderSurfingCompat: attack key held OR any holdable/chargeable skill.
    public static boolean needsCameraCoupling(LocalPlayer player) {
        return isAttackKeyActive() || isHoldingSkill(player);
    }

    private static boolean isLoaded = ModList.get().isLoaded("epicfight");

    public static boolean isLockOnTargeting() {
        if (!isLoaded) return false;
        try {
            return yesman.epicfight.api.client.camera.EpicFightCameraAPI.getInstance() != null &&
                   yesman.epicfight.api.client.camera.EpicFightCameraAPI.getInstance().isLockingOnTarget();
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean animationOwnsLivingMotion(LocalPlayer player) {
        if (!isLoaded || player == null) return false;
        try {
            yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch patch
                    = yesman.epicfight.world.capabilities.EpicFightCapabilities.getLocalPlayerPatch(player);
            if (patch == null) return false;
            return !patch.getEntityState().getState(
                    yesman.epicfight.api.animation.types.EntityState.UPDATE_LIVING_MOTION);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isWallClimbing(LocalPlayer player) {
        if (!isLoaded || player == null) return false;
        if (player.onGround()) return false;
        try {
            yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch patch
                    = yesman.epicfight.world.capabilities.EpicFightCapabilities.getLocalPlayerPatch(player);
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

    // True while WoM's Aqua Maneuver mover skill is the equipped mover and the player is sprint-swimming.
    public static boolean isWomMoverSwimming(LocalPlayer player) {
        if (!isLoaded || player == null) return false;
        if (!player.isInWater() || !player.isSprinting()) return false;
        try {
            yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch patch
                    = yesman.epicfight.world.capabilities.EpicFightCapabilities.getLocalPlayerPatch(player);
            if (patch == null) return false;
            yesman.epicfight.skill.SkillContainer mover = patch.getSkill(yesman.epicfight.skill.SkillSlots.MOVER);
            if (mover == null) return false;
            yesman.epicfight.skill.Skill skill = mover.getSkill();
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
