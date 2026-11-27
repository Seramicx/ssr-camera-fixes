package com.ssrcamerafixes.fabric.mixin;

import com.ssrcamerafixes.compat.PotatoCannonHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.fabric.handler.AimingFaceCameraHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// PotatoCannonItem.use spawns the projectile server side off player.getLookAngle(), and its useOn delegates
// straight back to use, so a shot aimed at a block in reach never passes through useItem.
@Mixin(MultiPlayerGameMode.class)
public abstract class MixinPotatoCannonFaceCamera {

    @Inject(method = "useItem", at = @At("HEAD"))
    private void ssrcamerafixes$faceCrosshairForPotatoCannon(Player player, InteractionHand hand,
                                                             CallbackInfoReturnable<?> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (player != mc.player) return;

        ssrcamerafixes$snapPotatoCannon(mc);
    }

    @Inject(method = "useItemOn", at = @At("HEAD"))
    private void ssrcamerafixes$faceCrosshairForPotatoCannonOnBlock(LocalPlayer player, InteractionHand hand,
                                                                    BlockHitResult hitResult,
                                                                    CallbackInfoReturnable<?> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (player != mc.player) return;

        ssrcamerafixes$snapPotatoCannon(mc);
    }

    private static void ssrcamerafixes$snapPotatoCannon(Minecraft mc) {
        LocalPlayer local = mc.player;
        if (local == null) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!PotatoCannonHelper.isHoldingPotatoCannon(local)) return;

        AimingFaceCameraHandler.faceCrosshairAndSync(mc, local);
    }
}
