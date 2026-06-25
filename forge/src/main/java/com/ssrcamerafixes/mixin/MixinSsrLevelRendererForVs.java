package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.compat.ValkyrienSkiesHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// On a VS ship SSR computes the shoulder offset but never moves the camera, so the player renders centered.
// Re-apply the shift along the camera basis after VS has positioned the camera for this frame.
@Mixin(LevelRenderer.class)
public abstract class MixinSsrLevelRendererForVs {

    @Inject(method = "prepareCullFrustum", at = @At("HEAD"))
    private void ssrcamerafixes$applyShoulderShiftOnVsMount(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (mc.options.getCameraType() != CameraType.THIRD_PERSON_BACK) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!ValkyrienSkiesHelper.isMountedOnShip(player)) return;

        Camera camera = mc.gameRenderer.getMainCamera();
        if (camera == null) return;

        Vec3 target = ShoulderSurfingHelper.getTargetOffset();
        double effectiveX = target.x();
        double effectiveY = target.y();

        // SSR zeroes its offset when it thinks you're looking down; fall back to the configured shoulder so the
        // shift stays visible on deck
        if (Math.abs(effectiveX) < 1.0E-4 && Math.abs(effectiveY) < 1.0E-4) {
            effectiveX = ShoulderSurfingHelper.getConfigOffsetX();
            effectiveY = ShoulderSurfingHelper.getConfigOffsetY();
        }

        if (Math.abs(effectiveX) < 1.0E-4 && Math.abs(effectiveY) < 1.0E-4) return;

        ((CameraMoveInvoker) camera).ssrcamerafixes$callMove(0.0, effectiveY, effectiveX);
    }
}
