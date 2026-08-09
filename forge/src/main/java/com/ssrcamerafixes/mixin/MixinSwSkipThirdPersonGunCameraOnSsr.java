package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.compat.SuperbWarfareHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// SW CameraMixin.superbWarfare$setup (TAIL of Camera.setup, 3P + gun + zoom/bow) does:
//   Camera.move(-getMaxZoom(-2.9*zoom), 0, -cameraLocation*zoom)
// cameraLocation static-inits to 0.6, so ADS always slides the *camera* without changing player
// lookAngle (bullets still use getLookAngle()). Under SSR that splits screen-center reticle from aim.
//
// Cancel SW's handler body under SSR (same early-return SW already does after the move).
// Omit SW's CallbackInfo arg from our signature so `ci` is *our* cancellable for this handler,
// not Camera.setup's CI. priority > SW so the handler method exists before we target it.
@Mixin(value = Camera.class, priority = 2000)
public abstract class MixinSwSkipThirdPersonGunCameraOnSsr {

    @Inject(
            method = "superbWarfare$setup",
            at = @At("HEAD"),
            cancellable = true,
            remap = false,
            require = 0
    )
    private void ssrcamerafixes$skipSwGunAdsCameraMove(CallbackInfo ci) {
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCameraType() != CameraType.THIRD_PERSON_BACK) return;
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (!SuperbWarfareHelper.isHoldingGun(player)) return;
        if (!SuperbWarfareHelper.hasAdsCameraPull()) return;
        ci.cancel();
    }
}
