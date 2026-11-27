package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.handler.UniversalAimHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MixinUniversalAimInput {

    @Inject(method = "startUseItem", at = @At("HEAD"))
    private void ssrcamerafixes$aimBeforeUse(CallbackInfo ci) {
        UniversalAimHandler.INSTANCE.beforeUse();
    }

    @Inject(method = "startAttack", at = @At("HEAD"))
    private void ssrcamerafixes$aimBeforeAttack(CallbackInfoReturnable<Boolean> cir) {
        UniversalAimHandler.INSTANCE.beforeAttack();
    }
}
