package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.compat.SuperbWarfareHelper;
import com.ssrcamerafixes.handler.AimingFaceCameraHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// SW sends a rotationless ShootMessage and the server shoots from the player's live lookAngle. While
// decoupled the body yaw is the shoulder offset, so face the crosshair before handleClientShoot sends the packet
@Pseudo
@Mixin(targets = "com.atsuishio.superbwarfare.event.ClientEventHandler", remap = false)
public abstract class MixinSuperbWarfareGunFaceCamera {

    @Inject(method = "handleClientShoot", at = @At("HEAD"), require = 0, remap = false)
    private void ssrcamerafixes$faceCrosshairForGun(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;

        // The server reads the yaw again when the bullet spawns, so sync the rotation with the shot and latch
        // so the tick handlers keep correcting until it resolves
        SuperbWarfareHelper.signalFire();
        AimingFaceCameraHandler.faceCrosshairAndSync(mc, player);
    }
}
