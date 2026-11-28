package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Method;

// Better Third Person takes any non-first-person view as its own, including SSR's shoulder view
@Pseudo
@Mixin(targets = "io.socol.betterthirdperson.api.CustomCameraManager", remap = false)
public abstract class MixinBtpStandDownForSsr {

    @Unique private static Method ssrcamerafixes$isFirstPerson;

    @Redirect(method = "mustHaveCustomCamera", at = @At(value = "INVOKE", target = "Lio/socol/betterthirdperson/api/adapter/IClientAdapter;isFirstPerson()Z", remap = false), require = 0, remap = false)
    private boolean ssrcamerafixes$ssrViewIsNotBtp(@Coerce Object client) {
        if (ShoulderSurfingHelper.isShoulderSurfingActive()) return true;
        try {
            if (ssrcamerafixes$isFirstPerson == null) {
                ssrcamerafixes$isFirstPerson = Class.forName("io.socol.betterthirdperson.api.adapter.IClientAdapter").getMethod("isFirstPerson");
            }
            return (boolean) ssrcamerafixes$isFirstPerson.invoke(client);
        } catch (Throwable t) {
            return false;
        }
    }
}
