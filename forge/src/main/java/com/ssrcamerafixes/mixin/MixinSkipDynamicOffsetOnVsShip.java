package com.ssrcamerafixes.mixin;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera;
import com.ssrcamerafixes.compat.ValkyrienSkiesHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// SSR's dynamic offset raycast is world-space, but a VS ship's blocks live in a separate physics level, so on
// certain yaws the ray clips the hull and SSR pulls the shoulder in. Skip the raycast while mounted on a ship.
@Pseudo
@Mixin(value = ShoulderSurfingCamera.class, remap = false)
public abstract class MixinSkipDynamicOffsetOnVsShip {

    @Inject(method = "calcDynamicOffsets", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void ssrcamerafixes$skipOnVsShip(Camera camera, Entity cameraEntity, BlockGetter level,
                                                    Vec3 offset, CallbackInfoReturnable<Vec3> cir) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && ValkyrienSkiesHelper.isMountedOnShip(player)) {
            cir.setReturnValue(offset);
        }
    }
}
