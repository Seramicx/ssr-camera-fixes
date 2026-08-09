package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ValkyrienSkiesHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// The rendered beam takes only its length from the server hit; the direction is recomputed here from the
// player euler angles, which are ship-local on a mount, so the visible laser misses even once the server
// raytrace is corrected. Both the muzzle origin and the look vector read the same fields, so swap them
// to world across the whole render.
@Pseudo
@Mixin(targets = "top.ribs.scguns.client.handler.BeamHandler", remap = false)
public abstract class MixinScorchedBeamRenderDirection {

    private static float ssrcamerafixes$savedYRot;
    private static float ssrcamerafixes$savedXRot;
    private static float ssrcamerafixes$savedYRotO;
    private static float ssrcamerafixes$savedXRotO;
    private static boolean ssrcamerafixes$swapped;

    @Inject(method = "renderBeam", at = @At("HEAD"), require = 0, remap = false)
    private static void ssrcamerafixes$toWorldRot(Player beamPlayer, @Coerce Object beamInfo,
                                                  @Coerce Object modifiedGun, ItemStack heldItem,
                                                  float partialTicks, com.mojang.blaze3d.vertex.PoseStack poseStack,
                                                  MultiBufferSource.BufferSource bufferSource, CallbackInfo ci) {
        ssrcamerafixes$swapped = false;
        if (beamPlayer == null || !ValkyrienSkiesHelper.isMountedOnShip(beamPlayer)) return;

        float[] now = ValkyrienSkiesHelper.shipLocalRotToWorldRot(beamPlayer, beamPlayer.getYRot(), beamPlayer.getXRot());
        float[] prev = ValkyrienSkiesHelper.shipLocalRotToWorldRot(beamPlayer, beamPlayer.yRotO, beamPlayer.xRotO);
        if (now == null || prev == null) return;

        ssrcamerafixes$savedYRot = beamPlayer.getYRot();
        ssrcamerafixes$savedXRot = beamPlayer.getXRot();
        ssrcamerafixes$savedYRotO = beamPlayer.yRotO;
        ssrcamerafixes$savedXRotO = beamPlayer.xRotO;
        ssrcamerafixes$swapped = true;
        beamPlayer.setYRot(now[0]);
        beamPlayer.setXRot(now[1]);
        beamPlayer.yRotO = prev[0];
        beamPlayer.xRotO = prev[1];
    }

    @Inject(method = "renderBeam", at = @At("RETURN"), require = 0, remap = false)
    private static void ssrcamerafixes$restoreRot(Player beamPlayer, @Coerce Object beamInfo,
                                                  @Coerce Object modifiedGun, ItemStack heldItem,
                                                  float partialTicks, com.mojang.blaze3d.vertex.PoseStack poseStack,
                                                  MultiBufferSource.BufferSource bufferSource, CallbackInfo ci) {
        if (!ssrcamerafixes$swapped || beamPlayer == null) return;
        ssrcamerafixes$swapped = false;
        beamPlayer.setYRot(ssrcamerafixes$savedYRot);
        beamPlayer.setXRot(ssrcamerafixes$savedXRot);
        beamPlayer.yRotO = ssrcamerafixes$savedYRotO;
        beamPlayer.xRotO = ssrcamerafixes$savedXRotO;
    }
}
