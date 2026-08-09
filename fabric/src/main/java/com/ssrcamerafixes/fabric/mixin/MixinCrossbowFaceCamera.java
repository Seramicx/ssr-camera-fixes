package com.ssrcamerafixes.fabric.mixin;

import com.ssrcamerafixes.compat.HeldCrossbowAim;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.fabric.handler.AimingFaceCameraHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// ServerboundUseItemPacket carries no rotation and useItem sends it before CrossbowItem.use runs, so the
// server shoots along whatever rotation was last synced. Decoupling the body left that at the shoulder yaw.
@Mixin(MultiPlayerGameMode.class)
public abstract class MixinCrossbowFaceCamera {

    @Inject(method = "useItem", at = @At("HEAD"))
    private void ssrcamerafixes$faceCrosshairForCrossbow(Player player, InteractionHand hand,
                                                         CallbackInfoReturnable<?> cir) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer local = mc.player;
        if (local == null || player != local) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!HeldCrossbowAim.isPassiveHold(local)) return;

        AimingFaceCameraHandler.faceCrosshairAndSync(mc, local);
    }
}
