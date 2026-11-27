package com.ssrcamerafixes.fabric1211.mixin;

import com.ssrcamerafixes.compat.JegHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.fabric1211.handler.AimingFaceCameraHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// JEG aims the bullet from last tick's rotation, so after a turn the shot waits for the server to catch up
@Pseudo
@Mixin(targets = "ttv.migami.jeg.network.ClientNetworkHandler", remap = false)
public abstract class MixinJegGunFaceCamera {

    @Inject(method = "sendShoot", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void ssrcamerafixes$faceCrosshairForGun(InteractionHand hand, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer local = mc.player;
        if (local == null || JegHelper.isResending()) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;

        JegHelper.signalFire();
        float yRotO = local.getYRot();
        float xRotO = local.getXRot();
        AimingFaceCameraHandler.faceCrosshairAndSync(mc, local);
        if (Math.abs(Mth.wrapDegrees(local.getYRot() - yRotO)) > 0.5F || Math.abs(local.getXRot() - xRotO) > 0.5F) {
            JegHelper.deferShot(hand);
            ci.cancel();
        }
    }
}
