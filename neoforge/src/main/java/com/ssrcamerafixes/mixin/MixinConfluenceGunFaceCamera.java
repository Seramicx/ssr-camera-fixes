package com.ssrcamerafixes.mixin;

import net.minecraft.util.Mth;
import com.ssrcamerafixes.compat.ConfluenceHelper;
import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ssrcamerafixes.compat.AimRules;
import com.ssrcamerafixes.handler.AimingFaceCameraHandler;

// Confluence aims the bullet from last tick's rotation, so after a turn the shot waits for the server to catch up
@Pseudo
@Mixin(targets = {"org.confluence.terra_guns.network.c2s.ShootPacketC2S", "org.confluence.mod.network.c2s.ShootPacketC2S"}, remap = false)
public abstract class MixinConfluenceGunFaceCamera {

    @Inject(method = "sendToServer", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void ssrcamerafixes$faceCrosshairForGun(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || ConfluenceHelper.isResending()) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;
        if (!ConfluenceHelper.isHoldingGun(mc.player)) return;

        ConfluenceHelper.signalFire();
        if (AimRules.isFreeLooking() || !AimRules.useTurnAllowed()) return;
        float yRotO = mc.player.getYRot();
        float xRotO = mc.player.getXRot();
        AimingFaceCameraHandler.faceCrosshairAndSync(mc, mc.player);
        if (Math.abs(Mth.wrapDegrees(mc.player.getYRot() - yRotO)) > 0.5F || Math.abs(mc.player.getXRot() - xRotO) > 0.5F) {
            ConfluenceHelper.deferShot();
            ci.cancel();
        }
    }
}
