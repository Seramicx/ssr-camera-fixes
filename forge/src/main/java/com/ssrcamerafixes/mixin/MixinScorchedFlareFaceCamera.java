package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.GunModHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.handler.AimingFaceCameraHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// FlarePistolItem.use spawns the flare along the player look on both sides, and useItem has already sent
// ServerboundUseItemPacket by the time the item method runs, so the snap has to happen before the send
@Mixin(MultiPlayerGameMode.class)
public abstract class MixinScorchedFlareFaceCamera {

    @Inject(method = "useItem", at = @At("HEAD"))
    private void ssrcamerafixes$faceCrosshairForFlare(net.minecraft.world.entity.player.Player player,
                                                      InteractionHand hand,
                                                      CallbackInfoReturnable<net.minecraft.world.InteractionResult> cir) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer local = mc.player;
        if (local == null || player != local) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;
        if (!GunModHelper.isHoldingFlarePistol(local)) return;

        AimingFaceCameraHandler.faceCrosshairAndSync(mc, local);
    }
}
