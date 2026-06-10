package com.ssrcamerafixes.mixin;

import com.github.exopandora.shouldersurfing.client.InputHandler;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(value = InputHandler.class, remap = false)
public abstract class MixinSuppressMovingInputYawFollow {

    @Redirect(
        method = "updateMovementInput",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;setYRot(F)V",
            remap = true
        ),
        require = 0,
        remap = false
    )
    private void ssrcamerafixes$movingInputSetYRot(LocalPlayer player, float yRot) {
        if (com.ssrcamerafixes.compat.EpicFightHelper.animationOwnsLivingMotion(player)) {
            return;
        }
        player.setYRot(yRot);
    }
}
