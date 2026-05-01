package com.ssrcamerafixes.mixin;

import com.github.exopandora.shouldersurfing.client.InputHandler;
import com.mojang.blaze3d.platform.InputConstants;
import com.ssrcamerafixes.SsrCameraFixesMod;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Drains SSR's {@code SWAP_SHOULDER} click queue at HEAD of
 * {@code InputHandler.tick} when our {@code SHOULDER_CYCLE} keybind is bound
 * to the same key. Without this, both keybinds fire on the same press -
 * SSR's {@code swapShoulder()} runs in addition to our cycle's calls,
 * producing a double-swap that desyncs visual state from toast text on the
 * RIGHT→LEFT and OVERHEAD→RIGHT transitions.
 *
 * <p>Gated on key collision so users who rebind one of the two avoid this
 * mod intercepting SSR's input.
 */
@Mixin(value = InputHandler.class, remap = false)
public abstract class MixinSuppressSsrSwapShoulderKeybind {

    @Inject(method = "tick", at = @At("HEAD"), require = 0, remap = false)
    private void ssrcamerafixes$drainSsrSwapShoulderClicks(CallbackInfo ci) {
        KeyMapping ourCycle = SsrCameraFixesMod.SHOULDER_CYCLE;
        if (ourCycle == null) return;

        InputConstants.Key ourKey = ourCycle.getKey();
        InputConstants.Key ssrKey;
        try {
            ssrKey = InputHandler.SWAP_SHOULDER.getKey();
        } catch (Throwable t) {
            return;
        }
        if (!ourKey.equals(ssrKey)) return;

        try {
            //noinspection StatementWithEmptyBody
            while (InputHandler.SWAP_SHOULDER.consumeClick()) {}
        } catch (Throwable ignored) {}
    }
}
