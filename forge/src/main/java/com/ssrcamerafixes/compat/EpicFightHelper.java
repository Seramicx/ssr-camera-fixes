package com.ssrcamerafixes.compat;

import com.ssrcamerafixes.SsrCameraFixesMod;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TridentItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;

@Mod.EventBusSubscriber(modid = SsrCameraFixesMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class EpicFightHelper {

    private static final long CAST_LATCH_MS = 500L;
    private static long lastCastSignalMs = 0L;

    private EpicFightHelper() {}

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

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!IronSpellsHelper.isLoaded()) return;

        if (IronSpellsHelper.anyCastKeymapDown() || IronSpellsHelper.isCasting()) {
            lastCastSignalMs = System.currentTimeMillis();
        }
    }

    private static boolean castLatchActive() {
        return (System.currentTimeMillis() - lastCastSignalMs) < CAST_LATCH_MS;
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
