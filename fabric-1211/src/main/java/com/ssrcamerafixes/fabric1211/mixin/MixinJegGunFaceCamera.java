package com.ssrcamerafixes.fabric1211.mixin;

import com.ssrcamerafixes.compat.JegHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.fabric1211.handler.AimingFaceCameraHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// The 1.21.1 port dropped the rotation fields from the shoot packet, so the server fires from whatever yaw was last
// synced. The shot is sent from a client tick handler, after the vanilla move packet already left, so turning alone
// leaves the server a tick behind. Face the crosshair and push the rotation ahead of the shoot packet
@Pseudo
@Mixin(targets = "ttv.migami.jeg.network.ClientNetworkHandler", remap = false)
public abstract class MixinJegGunFaceCamera {

    @Inject(method = "sendShoot", at = @At("HEAD"), require = 0, remap = false)
    private static void ssrcamerafixes$faceCrosshairForGun(InteractionHand hand, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer local = mc.player;
        if (local == null) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;

        JegHelper.signalFire();
        AimingFaceCameraHandler.faceCrosshairAndSync(mc, local);
    }
}
