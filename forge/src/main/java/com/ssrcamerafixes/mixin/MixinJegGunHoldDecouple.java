package com.ssrcamerafixes.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// JEG toggles SSR's real camera-coupling setting while the shoot key is held, which fights decoupling and never
// restores the setting once a gun has fired. It also resolves ShoulderSurfingImpl, gone since SSR v5, so on v5 that
// block throws every tick a loaded gun is held. Report the mod as absent so the whole thing is skipped
@Pseudo
@Mixin(targets = "ttv.migami.jeg.client.handler.ShootingHandler", remap = false)
public abstract class MixinJegGunHoldDecouple {

    @Redirect(method = "onHandleShooting",
            at = @At(value = "FIELD", target = "Lttv/migami/jeg/JustEnoughGuns;shoulderSurfingLoaded:Z", opcode = Opcodes.GETSTATIC),
            require = 0, remap = false)
    private boolean ssrcamerafixes$hideShoulderSurfingFromJeg() {
        return false;
    }
}
