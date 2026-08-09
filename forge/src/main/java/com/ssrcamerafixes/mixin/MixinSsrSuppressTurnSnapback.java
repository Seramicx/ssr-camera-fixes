package com.ssrcamerafixes.mixin;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera;
import com.ssrcamerafixes.compat.BetterMountSteeringHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = ShoulderSurfingCamera.class, remap = false)
public abstract class MixinSsrSuppressTurnSnapback {

    @Unique private boolean ssrcamerafixes$shouldRestore;
    @Unique private float ssrcamerafixes$savedYRot;
    @Unique private float ssrcamerafixes$savedXRot;
    @Unique private float ssrcamerafixes$savedYRotO;
    @Unique private float ssrcamerafixes$savedXRotO;
    @Unique private static int ssrcamerafixes$aimDbgCooldown;

    @Inject(
        method = "turn(Lnet/minecraft/client/player/LocalPlayer;DD)Z",
        at = @At("HEAD"),
        require = 0,
        remap = false
    )
    private void ssrcamerafixes$beforeTurn(LocalPlayer player, double yRot, double xRot,
                                           CallbackInfoReturnable<Boolean> cir) {
        ssrcamerafixes$shouldRestore =
                BetterMountSteeringHelper.isDecoupleTransitioning()
                && player == Minecraft.getInstance().player;
        if (ssrcamerafixes$shouldRestore) {
            ssrcamerafixes$savedYRot  = player.getYRot();
            ssrcamerafixes$savedXRot  = player.getXRot();
            ssrcamerafixes$savedYRotO = player.yRotO;
            ssrcamerafixes$savedXRotO = player.xRotO;
        }
    }

    @Inject(
        method = "turn(Lnet/minecraft/client/player/LocalPlayer;DD)Z",
        at = @At("RETURN"),
        require = 0,
        remap = false
    )
    private void ssrcamerafixes$afterTurn(LocalPlayer player, double yRot, double xRot,
                                          CallbackInfoReturnable<Boolean> cir) {
        if (!ssrcamerafixes$shouldRestore) return;
        float yAfterSsr = player.getYRot();
        float xAfterSsr = player.getXRot();
        player.setYRot(ssrcamerafixes$savedYRot);
        player.setXRot(ssrcamerafixes$savedXRot);
        player.yRotO = ssrcamerafixes$savedYRotO;
        player.xRotO = ssrcamerafixes$savedXRotO;
        ssrcamerafixes$shouldRestore = false;
        // AIMDBG: temporary — proves BMS-compat undoes SSR aim coupling. Remove after diagnosis.
        if (player.isUsingItem() && ssrcamerafixes$aimDbgCooldown-- <= 0) {
            ssrcamerafixes$aimDbgCooldown = 5;
            org.slf4j.LoggerFactory.getLogger("AIMDBG").info(
                    "SuppressTurnSnapback FIRED using={} mounted={} ySsr={}→restored={} xSsr={}→restored={}",
                    true,
                    player.getVehicle() != null,
                    yAfterSsr, player.getYRot(),
                    xAfterSsr, player.getXRot());
        }
    }
}
