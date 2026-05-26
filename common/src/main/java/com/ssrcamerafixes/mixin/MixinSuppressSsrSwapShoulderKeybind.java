package com.ssrcamerafixes.mixin;

import com.github.exopandora.shouldersurfing.client.InputHandler;
import com.mojang.blaze3d.platform.InputConstants;
import com.ssrcamerafixes.Keybinds;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InputHandler.class, remap = false)
public abstract class MixinSuppressSsrSwapShoulderKeybind {

    @Inject(method = "tick", at = @At("HEAD"), require = 0, remap = false)
    private void ssrcamerafixes$drainSsrSwapShoulderClicks(CallbackInfo ci) {
        KeyMapping ourCycle = Keybinds.SHOULDER_CYCLE;
        if (ourCycle == null) return;

        InputConstants.Key ourKey = ((AccessorKeyMapping) (Object) ourCycle).ssrcamerafixes$getKey();
        InputConstants.Key ssrKey;
        try {
            ssrKey = ((AccessorKeyMapping) (Object) InputHandler.SWAP_SHOULDER).ssrcamerafixes$getKey();
        } catch (Throwable t) {
            return;
        }
        if (!ourKey.equals(ssrKey)) return;

        try {
            while (InputHandler.SWAP_SHOULDER.consumeClick()) {}
        } catch (Throwable ignored) {}
    }
}
