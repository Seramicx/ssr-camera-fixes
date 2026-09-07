package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// JEG's own camera recoil reads ShoulderSurfingImpl, so it silently stops working on SSR v5, and on v4 it also
// overwrites the pitch/yaw it then pushes the body with, snapping the body to the camera on every shot. Take over
// the camera half here and return false so JEG's body push keeps using the player's own rotation
@Pseudo
@Mixin(targets = "ttv.migami.jeg.client.handler.GunRecoilHandler", remap = false)
public abstract class MixinJegRecoilCameraPush {

    @Shadow(remap = false) private float cameraRecoil;
    @Shadow(remap = false) private float progressCameraRecoil;
    @Shadow(remap = false) private static int recoilRand;

    @Redirect(method = "onRenderTick",
            at = @At(value = "INVOKE", target = "Lttv/migami/jeg/compat/ShoulderSurfingHelper;isShoulderSurfing()Z"),
            require = 0, remap = false)
    private boolean ssrcamerafixes$pushCameraRecoil() {
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return false;

        float delta = Minecraft.getInstance().getDeltaFrameTime() * 0.15F;
        float startProgress = this.progressCameraRecoil / this.cameraRecoil;
        float phase = startProgress < 0.2F ? 0.2F : 0.8F;
        float pitchKick = (startProgress < 0.2F ? -delta : delta) / phase * this.cameraRecoil;
        float yawKick = recoilRand == 1 ? pitchKick / 2 : -pitchKick / 2;

        ShoulderSurfingHelper.setCameraRotation(
                ShoulderSurfingHelper.getCameraYaw() + yawKick,
                ShoulderSurfingHelper.getCameraXRot() + pitchKick);
        return false;
    }
}
