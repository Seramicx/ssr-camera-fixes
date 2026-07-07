package com.ssrcamerafixes.mixin;

import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// handleShoot sets the body yaw from the packet but leaves the server head yaw stale. CGM's ProjectileEntity
// reads getYHeadRot() for any gun with spread, so the shot rakes off the crosshair (worse on a mount, where the
// head yaw is the shoulder offset). Mirror the just-applied body yaw into head yaw before the projectile spawns.
// The 1.20.1 CGM fork switched the spread path to getYRot(), which is why only 1.19.2 needs this
@Pseudo
@Mixin(targets = "com.mrcrayfish.guns.common.network.ServerPlayHandler", remap = false)
public abstract class MixinCgmGunServerHeadYaw {

    @Inject(
        method = "handleShoot",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mrcrayfish/guns/network/message/C2SMessageShoot;getRotationPitch()F",
            remap = false
        ),
        require = 0,
        remap = false
    )
    private static void ssrcamerafixes$alignHeadYaw(@Coerce Object message, ServerPlayer player, CallbackInfo ci) {
        player.setYHeadRot(player.getYRot());
    }
}
