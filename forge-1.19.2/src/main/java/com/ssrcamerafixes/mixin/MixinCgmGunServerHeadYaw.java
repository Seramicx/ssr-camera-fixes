package com.ssrcamerafixes.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// handleShoot sets body yaw from the packet but leaves server head yaw stale. CGM's ProjectileEntity
// reads getYHeadRot() for any gun with spread, so the shot rakes off the crosshair (worse on a mount).
// Mirror body → head after the packet rotations are applied. 1.20.1 CGM uses getYRot() for spread.
@Pseudo
@Mixin(targets = "com.mrcrayfish.guns.common.network.ServerPlayHandler", remap = false)
public abstract class MixinCgmGunServerHeadYaw {

    @Inject(method = "handleShoot", at = @At("HEAD"), require = 0, remap = false)
    private static void ssrcamerafixes$forceHeadYaw(@Coerce Object message, ServerPlayer player, CallbackInfo ci) {
        float pktYaw = Float.NaN;
        try {
            pktYaw = (float) message.getClass().getMethod("getRotationYaw").invoke(message);
        } catch (Throwable ignored) {}
        if (!Float.isNaN(pktYaw)) {
            player.setYHeadRot(pktYaw);
            player.yHeadRotO = pktYaw;
        }
    }

    // After setXRot from the shoot packet — body yaw/pitch are final; align head before getDirection.
    @Inject(
        method = "handleShoot",
        at = {
            @At(
                value = "INVOKE",
                target = "Lcom/mrcrayfish/guns/network/message/C2SMessageShoot;getRotationPitch()F",
                remap = false
            ),
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/server/level/ServerPlayer;m_146926_(F)V",
                shift = At.Shift.AFTER,
                remap = false
            ),
            @At(
                value = "INVOKE",
                target = "Lnet/minecraft/server/level/ServerPlayer;setXRot(F)V",
                shift = At.Shift.AFTER,
                remap = true
            )
        },
        require = 0,
        remap = false
    )
    private static void ssrcamerafixes$alignHeadYaw(@Coerce Object message, ServerPlayer player, CallbackInfo ci) {
        player.setYHeadRot(player.getYRot());
    }
}
