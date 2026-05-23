package com.ssrcamerafixes.fabric.mixin;

import com.ssrcamerafixes.fabric.handler.SprintRotateHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Injects after Input.tick inside LocalPlayer.aiStep so impulses are populated
// but movement hasn't been applied yet.
@Mixin(LocalPlayer.class)
public abstract class MixinSprintRotateInput {

    @Inject(
            method = "aiStep",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/player/Input;tick(ZF)V",
                    shift = At.Shift.AFTER
            )
    )
    private void ssrcamerafixes$afterInputTick(CallbackInfo ci) {
        LocalPlayer self = (LocalPlayer) (Object) this;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != self) return;
        SprintRotateHandler.applyAfterInputTick(mc, self.input);
    }
}
