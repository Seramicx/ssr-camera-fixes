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

// 1.19.2 Scorched Guns (net.ribs.sc.scorchedguns) is a CGM addon — both fire through this handler.
@Pseudo
@Mixin(targets = "com.mrcrayfish.guns.client.handler.ShootingHandler", remap = false)
public abstract class MixinCgmGunFaceCamera {

    @Inject(method = "fire", at = @At("HEAD"), require = 0, remap = false)
    private void ssrcamerafixes$faceCrosshairForGun(Player player, ItemStack heldItem, CallbackInfo ci) {
        if (player != Minecraft.getInstance().player) return;
        if (Minecraft.getInstance().options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;
        ShoulderSurfingHelper.lookAtCrosshairTarget();
    }
}
