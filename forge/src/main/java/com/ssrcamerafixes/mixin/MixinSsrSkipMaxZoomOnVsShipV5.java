package com.ssrcamerafixes.mixin;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera;
import com.ssrcamerafixes.compat.ValkyrienSkiesHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// SSR maxZoom also raycasts along the offset using the (VS-transformed) camera basis. Same turn-direction
// jitter as VS's ship zoom. On a mount, keep the full offset length.
@Pseudo
@Mixin(value = ShoulderSurfingCamera.class, remap = false)
public abstract class MixinSsrSkipMaxZoomOnVsShipV5 {

    @Inject(method = "maxZoom", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void ssrcamerafixes$skipMaxZoomOnShip(Camera camera, Entity entity, BlockGetter level,
                                                         Vec3 offset, float partialTick,
                                                         CallbackInfoReturnable<Double> cir) {
        if (!(entity instanceof LocalPlayer player)) return;
        if (!ValkyrienSkiesHelper.isMountedOnShip(player)) return;
        cir.setReturnValue(offset.length());
    }
}
