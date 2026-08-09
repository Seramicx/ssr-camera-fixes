package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.compat.ValkyrienSkiesHelper;
import com.ssrcamerafixes.handler.AimingFaceCameraHandler;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.github.exopandora.shouldersurfing.api.util.EntityHelper", remap = false)
public abstract class MixinEntityHelperLookAtOnVsShip {

    @Inject(method = "lookAtTarget", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void ssrcamerafixes$shipLocalAim(LocalPlayer player, Vec3 target, CallbackInfo ci) {
        if (player == null || !ValkyrienSkiesHelper.isMountedOnShip(player)) return;
        AimingFaceCameraHandler.faceCrosshair(player);
        ShoulderSurfingHelper.setLastMovedYRot(player.getYRot());
        ci.cancel();
    }
}
