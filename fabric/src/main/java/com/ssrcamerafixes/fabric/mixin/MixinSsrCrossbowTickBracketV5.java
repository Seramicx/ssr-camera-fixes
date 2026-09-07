package com.ssrcamerafixes.fabric.mixin;

import com.ssrcamerafixes.fabric.handler.ChargedCrossbowDecoupleHandler;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Fabric has no listener priorities and SSR ticks from HEAD of Minecraft.tick, so two
// START_CLIENT_TICK callbacks can never straddle it. Bracket SSR's own tick instead.
@Pseudo
@Mixin(targets = "com.github.exopandora.shouldersurfing.client.ShoulderSurfing", remap = false)
public abstract class MixinSsrCrossbowTickBracketV5 {

    @Inject(method = "tick", at = @At("HEAD"), require = 0, remap = false)
    private void ssrcamerafixes$saveCrossbowRotation(CallbackInfo ci) {
        ChargedCrossbowDecoupleHandler.INSTANCE.onClientTickStart(Minecraft.getInstance());
    }

    @Inject(method = "tick", at = @At("RETURN"), require = 0, remap = false)
    private void ssrcamerafixes$restoreCrossbowRotation(CallbackInfo ci) {
        ChargedCrossbowDecoupleHandler.INSTANCE.onClientTickAfterSsr(Minecraft.getInstance());
    }
}
