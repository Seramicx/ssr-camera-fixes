package com.ssrcamerafixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// JEG 0.14.5 calls the SSR 4 ShoulderSurfingImpl here and crashes on SSR 5
@Pseudo
@Mixin(targets = "ttv.migami.jeg.client.handler.ShootingHandler", remap = false)
public abstract class MixinJustEnoughGunsSsr4Crash {

    @Redirect(method = "onHandleShooting", at = @At(value = "FIELD", target = "Lttv/migami/jeg/JustEnoughGuns;shoulderSurfingLoaded:Z", opcode = 178), require = 0, remap = false)
    private boolean ssrcamerafixes$skipSsr4Camera() {
        return false;
    }
}
