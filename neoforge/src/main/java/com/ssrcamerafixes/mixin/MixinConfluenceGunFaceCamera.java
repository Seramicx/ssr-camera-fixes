package com.ssrcamerafixes.mixin;

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

// TerraGuns fires via ShootPacketC2S which carries no rotation; the server shoots from the player's current yaw.
// A tick handler races the packet, so face the crosshair right here before the packet is dispatched
@Pseudo
@Mixin(targets = "org.confluence.terra_guns.network.c2s.ShootPacketC2S", remap = false)
public abstract class MixinConfluenceGunFaceCamera {

    @Inject(method = "sendToServer", at = @At("HEAD"), require = 0, remap = false)
    private static void ssrcamerafixes$faceCrosshairForGun(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;
        if (!ConfluenceHelper.isHoldingGun(mc.player)) return;

        ConfluenceHelper.signalFire();
        ShoulderSurfingHelper.lookAtCrosshairTarget();
    }
}
