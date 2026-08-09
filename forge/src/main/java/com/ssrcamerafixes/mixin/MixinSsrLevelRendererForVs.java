package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.BetterMountSteeringHelper;
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

// VS rebases the camera into ship space with a straight-back zoom, dropping SSR's shoulder shift, so re-apply
// it here. Use SSR's *lerped* render/offset (not targetOffset) so shoulder-cycle RIGHT↔LEFT↔OVERHEAD stays
// smooth — target jumps instantly when we force stable defaultOffset on ships, which felt like a snap.
// Fall back to config if the lerped offset was zeroed (looking-down center). Skip during BMS mount-rotate.
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
        if (BetterMountSteeringHelper.isMountRotateActive()
                || BetterMountSteeringHelper.isDecoupleTransitioning()) {
            return;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        if (camera == null) return;

        Vec3 lerped = ShoulderSurfingHelper.getRenderOffset();
        if (Math.abs(lerped.x()) < 1.0E-4 && Math.abs(lerped.y()) < 1.0E-4) {
            lerped = ShoulderSurfingHelper.getOffset();
        }
        double effectiveX = lerped.x();
        double effectiveY = lerped.y();
        if (Math.abs(effectiveX) < 1.0E-4 && Math.abs(effectiveY) < 1.0E-4) {
            Vec3 target = ShoulderSurfingHelper.getTargetOffset();
            effectiveX = target.x();
            effectiveY = target.y();
        }
        if (Math.abs(effectiveX) < 1.0E-4 && Math.abs(effectiveY) < 1.0E-4) {
            effectiveX = ShoulderSurfingHelper.getConfigOffsetX();
            effectiveY = ShoulderSurfingHelper.getConfigOffsetY();
        }
        if (Math.abs(effectiveX) < 1.0E-4 && Math.abs(effectiveY) < 1.0E-4) return;

        ((CameraMoveInvoker) camera).ssrcamerafixes$callMove(0.0, effectiveY, effectiveX);
    }
}
