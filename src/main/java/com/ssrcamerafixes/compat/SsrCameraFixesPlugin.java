package com.ssrcamerafixes.compat;

import com.github.exopandora.shouldersurfing.api.callback.IPlayerInputCallback;
import com.github.exopandora.shouldersurfing.api.callback.ITargetCameraOffsetCallback;
import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingPlugin;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingRegistrar;
import com.ssrcamerafixes.SsrCameraFixesConfig;
import com.ssrcamerafixes.handler.ShoulderCycleHandler;
import com.ssrcamerafixes.handler.SprintRotateHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/**
 * SSR plugin entry point. Discovered by SSR via {@code shouldersurfing_plugin.json}.
 *
 * <p>Registers two callbacks:
 * <ul>
 *   <li>{@link OverheadOffsetCallback} - overrides target offset to
 *       {@code (0, overheadY, original.z)} when the shoulder cycle is in
 *       OVERHEAD mode.
 *   <li>{@link ForceVanillaInputCallback} - tells SSR to use vanilla movement
 *       input (skip its own input rewriting) whenever this mod's handlers
 *       are the input authority. Without this, SSR's input handling fights
 *       sprint-rotate / lock-on rotations.
 * </ul>
 */
public class SsrCameraFixesPlugin implements IShoulderSurfingPlugin {

    @Override
    public void register(IShoulderSurfingRegistrar registrar) {
        registrar.registerTargetCameraOffsetCallback(new OverheadOffsetCallback());
        registrar.registerPlayerInputCallback(new ForceVanillaInputCallback());
    }

    private static final class OverheadOffsetCallback implements ITargetCameraOffsetCallback {
        @Override
        public Vec3 post(IShoulderSurfing instance, Vec3 targetOffset, Vec3 defaultOffset) {
            if (ShoulderCycleHandler.getMode() != ShoulderCycleHandler.Mode.OVERHEAD) {
                return targetOffset;
            }

            // Pass through if SSR has collapsed the offset to (0, 0, z) - that
            // means SSR's "center camera when looking down" feature
            // (ShoulderSurfingCamera.calcOffset:270-273) kicked in. We don't
            // want to fight it: pillar-up assist needs the camera centered, and
            // bow aim needs the camera close to the player eye to avoid
            // overhead-Y parallax (camera target diverges from arrow target).
            // Both work correctly when we let SSR's centering through.
            //
            // OVERHEAD's defaultOffset.x is non-zero (typically -0.75 from the
            // user's config), so the (target.x ≈ 0) check reliably distinguishes
            // SSR-centered from pre-centered states.
            if (Math.abs(targetOffset.x) < 1.0E-4 && Math.abs(targetOffset.y) < 1.0E-4) {
                return targetOffset;
            }

            double overheadY;
            try {
                overheadY = SsrCameraFixesConfig.CAMERA_OVERHEAD_OFFSET_Y.get();
            } catch (Exception e) {
                overheadY = 1.2;
            }
            return new Vec3(0.0, overheadY, targetOffset.z);
        }
    }

    private static final class ForceVanillaInputCallback implements IPlayerInputCallback {
        @Override
        public boolean isForcingVanillaMovementInput(IsForcingVanillaMovementInputContext ctx) {
            // Lock-on: Mod 1's LockOnMovementHandler owns input.
            if (com.ssrcamerafixes.compat.EpicFightHelper.isLockOnTargeting()) {
                return true;
            }
            // Sprint-rotate: this mod owns input.
            if (SprintRotateHandler.isActive()) {
                return true;
            }
            // Sprinting in TPS without lock-on: also let our sprint-rotate take over
            // (matches the v2.0.0 behavior - keep SSR out of the way during sprints).
            Minecraft mc = ctx.minecraft();
            LocalPlayer player = mc != null ? mc.player : null;
            if (player != null
                    && player.isSprinting()
                    && mc.options.getCameraType() == CameraType.THIRD_PERSON_BACK) {
                return true;
            }
            return false;
        }
    }
}
