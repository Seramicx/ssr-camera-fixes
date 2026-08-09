package com.ssrcamerafixes.mixin;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera;
import com.ssrcamerafixes.compat.ValkyrienSkiesHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// v4 maxZoom takes no entity, so read the player off Minecraft instead
@Pseudo
@Mixin(value = ShoulderSurfingCamera.class, remap = false)
public abstract class MixinSsrSkipMaxZoomOnVsShipV4 {

    @Inject(method = "maxZoom", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void ssrcamerafixes$skipMaxZoomOnShip(Camera camera, BlockGetter level, Vec3 offset,
                                                         float partialTick,
                                                         CallbackInfoReturnable<Double> cir) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!ValkyrienSkiesHelper.isMountedOnShip(player)) return;
        cir.setReturnValue(offset.length());
    }
}
