package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.SsrCameraFixesMod;
import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Continuously drive {@code player.yRot}, {@code yBodyRot}, and
 * {@code yHeadRot} to the camera direction while the player is aiming a
 * ranged weapon, holding/using an Iron's Spells item, actively casting, or
 * blocking with a shield - so the body and head both face the crosshair for
 * the entire interaction with no visible lag between camera and character.
 *
 * <p>Writing all three (not just yRot) is required because vanilla never
 * updates {@code yHeadRot} per-tick client-side for the LocalPlayer
 * ({@code Player.serverAiStep} only fires when {@code isEffectiveAi()},
 * i.e. server-side). Without writing yHeadRot ourselves the head would
 * freeze at its last value while yBodyRot drifts, producing visible
 * head-vs-body twist during the action.
 *
 * <p>This only matters when SSR is loaded with a decoupled camera. Without
 * SSR, EpicFight already aligns yRot to the camera's hit-point each tick.
 * With SSR, {@code player.yRot} is the body direction, not the camera, so
 * the body wouldn't follow the crosshair without this fix.
 *
 * <p>Skipped while locked on - Mod 1's LockOnMovementHandler owns yRot during
 * lock-on (and EpicFight aligns to target there).
 */
@Mod.EventBusSubscriber(modid = SsrCameraFixesMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AimingFaceCameraHandler {

    private static final Minecraft MC = Minecraft.getInstance();

    private AimingFaceCameraHandler() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onMovementInput(MovementInputUpdateEvent event) {
        LocalPlayer player = MC.player;
        if (player == null) return;

        if (EpicFightHelper.isLockOnTargeting()) return;
        if (MC.options.getCameraType() == CameraType.FIRST_PERSON) return;
        // SSR-only: without SSR, EpicFight (and vanilla aiming) already aligns
        // yRot to the crosshair. Forcing yRot to mainCamera.getYRot() here in
        // vanilla 3rd person would clobber that and stall the camera one
        // render frame behind the mouse.
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!EpicFightHelper.isAiming(player) && !player.isBlocking()) return;

        float camYaw = ShoulderSurfingHelper.getCameraYaw();
        player.setYRot(camYaw);
        player.yBodyRot = camYaw;
        player.yHeadRot = camYaw;
    }
}
