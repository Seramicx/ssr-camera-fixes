package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Scorched Guns builds C2SMessageShoot from player.getYRot()/getXRot() at fire() and the server shoots from that
// packet rotation, ignoring synced look. While decoupled the body yaw is the shoulder offset, so face the
// crosshair here before the packet captures the rotation
@Pseudo
@Mixin(targets = "top.ribs.scguns.client.handler.ShootingHandler", remap = false)
public abstract class MixinScorchedGunsFaceCamera {

    @Inject(method = "fire", at = @At("HEAD"), require = 0, remap = false)
    private void ssrcamerafixes$faceCrosshairForGun(Player player, ItemStack heldItem, CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (player != mc.player) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;

        ShoulderSurfingHelper.lookAtCrosshairTarget();
    }
}
