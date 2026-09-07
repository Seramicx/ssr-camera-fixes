package com.ssrcamerafixes.fabric1211.mixin;

import com.ssrcamerafixes.fabric1211.handler.ChargedCrossbowDecoupleHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "com.github.exopandora.shouldersurfing.client.ShoulderSurfingImpl", remap = false)
public abstract class MixinSsrCrossbowTickBracketV4 {

    @Inject(method = "tick", at = @At("HEAD"), require = 0, remap = false)
    private void ssrcamerafixes$saveCrossbowRotation(CallbackInfo ci) {
        ChargedCrossbowDecoupleHandler.INSTANCE.onClientTickStart(Minecraft.getInstance());
    }

    @Inject(method = "tick", at = @At("RETURN"), require = 0, remap = false)
    private void ssrcamerafixes$restoreCrossbowRotation(CallbackInfo ci) {
        ChargedCrossbowDecoupleHandler.INSTANCE.onClientTickAfterSsr(Minecraft.getInstance());
    }
}
