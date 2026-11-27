package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.AimRules;
import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.handler.AimingFaceCameraHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Artifice cancels the vanilla attack and sends getLookAngle() in its own fire packet
@Pseudo
@Mixin(targets = "io.redspace.irons_artifice.client.InputHandler", remap = false)
public abstract class MixinArtificeGunFaceCamera {

    @Inject(method = "tryFire", at = @At("HEAD"), require = 0, remap = false)
    private static void ssrcamerafixes$faceCrosshairForGun(@Coerce Object profile, LocalPlayer player, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (player != mc.player) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;
        if (!AimRules.useTurnAllowed()) return;

        AimingFaceCameraHandler.faceCrosshairAndSync(mc, player);
    }
}
