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

/**
 * Epic Fight helper + Iron's Spells cast latch.
 *
 * <p>The latch gives a brief grace window after any cast signal drops so the
 * AimingFaceCameraHandler doesn't unlock body-to-camera between cast ticks.
 */
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

    /**
     * True when an EpicFight animation has explicitly disabled the normal
     * LivingMotion update loop ({@code EntityState.UPDATE_LIVING_MOTION = false}).
     *
     * <p>That flag is the cleanest "an animation owns the body, don't touch
     * {@code player.yRot}" signal we have:
     * <ul>
     *   <li>Default is {@code true} — vanilla sprint, walk, idle, jump are all unaffected.</li>
     *   <li>EpicFight's own {@code BIPED_SPRINT_JUMP} keeps it {@code true}, so sprint-rotate over a sprint-jump still works.</li>
     *   <li>WOM's spider techniques wall-run animations (WALL_RUNNING, WALL_GLIDE, WALL_RUN_LEFT_SIDE, WALL_RUN_RIGHT_SIDE) set it to {@code false}. These read {@code yBodyRot} to compute the wall-detection ray; if {@code SprintRotateHandler} rewrites {@code yRot} toward the camera, {@code tickHeadTurn} lerps {@code yBodyRot} off-wall and the climb breaks.</li>
     * </ul>
     */
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

    /** True when the player is drawing a ranged weapon, holding/using an Iron's item, or actively casting. */
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
