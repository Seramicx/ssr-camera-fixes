package com.ssrcamerafixes.fabric.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.fabric.compat.WizardsHelper;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(LocalPlayer.class)
public abstract class MixinSpellEngineCastToCrosshair {

    @Inject(method = "updateSpellCast", at = @At("HEAD"), require = 0, remap = false)
    private void ssrcamerafixes$aimSpellAtCrosshair(CallbackInfo ci) {
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!WizardsHelper.isCastingLive()) return;
        if (WizardsHelper.isInstantCasting() && isRidingMount((LocalPlayer) (Object) this)) return;
        ShoulderSurfingHelper.lookAtCrosshairTarget();
    }

    private static boolean isRidingMount(LocalPlayer player) {
        return player.getVehicle() != null;
    }
}
