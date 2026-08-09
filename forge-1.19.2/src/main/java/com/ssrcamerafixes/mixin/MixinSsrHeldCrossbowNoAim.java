package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.HeldCrossbowAim;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The adaptive-item callbacks are OR'd, so returning false from one can't cancel SSR's own config match.
// Targets PlayerStateHelper rather than ShoulderSurfingImpl.computeIsAiming (its only caller) because
// ShoulderSurfingImpl can get classloaded before mixin prepare.
@Pseudo
@Mixin(targets = "com.github.exopandora.shouldersurfing.client.PlayerStateHelper", remap = false)
public abstract class MixinSsrHeldCrossbowNoAim {

    @Inject(method = "isHoldingAdaptiveItem", at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private static void ssrcamerafixes$ignoreHeldCrossbow(Minecraft minecraft, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() && HeldCrossbowAim.isPassiveHold(entity)) {
            cir.setReturnValue(false);
        }
    }
}
