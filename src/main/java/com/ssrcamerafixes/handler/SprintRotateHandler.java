package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.SsrCameraFixesMod;
import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Sprint-rotate (non-lock-on, third-person, sprinting on foot).
 *
 * <p>Body runs in the direction of input while the camera stays where the
 * mouse pointed. We override player.yRot to the body direction during the
 * tick (so vanilla travel + sprint check both pass), then restore yRot at
 * PlayerTickEvent.END so the camera returns to the user's aim.
 *
 * <p>Without SSR, vanilla 3rd person already snaps body to movement direction.
 * With SSR, player.yRot is decoupled from the camera, so we have to
 * synthesize the body-vs-camera split ourselves.
 */
@Mod.EventBusSubscriber(modid = SsrCameraFixesMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SprintRotateHandler {

    private static final Minecraft MC = Minecraft.getInstance();

    private static boolean active = false;
    private static float savedYaw = 0F;

    private SprintRotateHandler() {}

    /** Read by the SSR plugin's input callback so SSR defers to our input rewriting. */
    public static boolean isActive() { return active; }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onMovementInput(MovementInputUpdateEvent event) {
        LocalPlayer player = MC.player;
        if (player == null) return;

        active = false;

        // Lock-on path takes priority - Mod 1's LockOnMovementHandler owns input there.
        if (EpicFightHelper.isLockOnTargeting()) return;
        if (MC.options.getCameraType() != CameraType.THIRD_PERSON_BACK) return;
        // SSR-only: in vanilla 3rd person back, player.yRot IS the camera, so
        // toggling it mid-tick (and restoring at PlayerTickEvent.END from a
        // one-frame-stale mainCamera.getYRot) makes the camera oscillate and
        // lag the mouse by a tick. Vanilla MC already snaps body to movement
        // direction natively, so this whole handler is only needed when SSR
        // has decoupled the body from the camera.
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!player.isSprinting()) return;

        // Aiming/casting/using-item/blocking: body must lock to camera direction
        // so projectiles fly at the crosshair. AimingFaceCameraHandler runs
        // separately and handles those cases.
        if (EpicFightHelper.isAiming(player) || player.isUsingItem() || player.isBlocking()) return;

        Input input = event.getInput();

        float rawForward = 0F;
        if (MC.options.keyUp.isDown()) rawForward += 1F;
        if (MC.options.keyDown.isDown()) rawForward -= 1F;

        float rawStrafe = 0F;
        if (MC.options.keyLeft.isDown()) rawStrafe += 1F;
        if (MC.options.keyRight.isDown()) rawStrafe -= 1F;

        float rawMagnitude = Mth.sqrt(rawForward * rawForward + rawStrafe * rawStrafe);
        if (rawMagnitude < 0.01F) return;

        // Read where the user is *looking* - with SSR, player.yRot is the body
        // direction, not the camera. SSR exposes its own camera yaw via
        // getCameraYaw(); fall back to player.yRot when SSR isn't installed
        // or in coupled mode (where they're equal anyway).
        float cameraYaw = ShoulderSurfingHelper.getCameraYaw();
        float offsetAngle = -(float) Math.toDegrees(Math.atan2(rawStrafe, rawForward));
        float bodyYaw = Mth.wrapDegrees(cameraYaw + offsetAngle);

        savedYaw = cameraYaw;
        player.setYRot(bodyYaw);

        // forwardImpulse positive (>= 0.8 sprint threshold) keeps MC's sprint
        // check happy. min(...) preserves any prior mod-applied slowdown.
        float modMagnitude = Mth.sqrt(input.forwardImpulse * input.forwardImpulse
                + input.leftImpulse * input.leftImpulse);
        float magnitude = Math.min(rawMagnitude, modMagnitude);
        input.forwardImpulse = magnitude;
        input.leftImpulse = 0F;

        active = true;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!event.side.isClient()) return;

        LocalPlayer player = MC.player;
        if (player == null || event.player != player) return;

        if (active) {
            // Restore the user's camera yaw. Vanilla tickHeadTurn already
            // smoothed yBodyRot toward the body direction; we just put yRot
            // back so the camera returns to the user's mouse aim.
            player.setYRot(savedYaw);
            player.yRotO = savedYaw;
            // Head follows body (movement direction), not camera. Otherwise
            // running backward looks weird.
            player.yHeadRot = player.yBodyRot;
            player.yHeadRotO = player.yBodyRotO;
            active = false;
        }
    }
}
