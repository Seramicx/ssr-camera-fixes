package com.ssrcamerafixes.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// This mod handles Better Third Person and Valkyrien Skies, so SSR's startup warning for them is dropped
@Pseudo
@Mixin(targets = "com.github.exopandora.shouldersurfing.forge.ShoulderSurfingForge", remap = false)
public abstract class MixinSsrIncompatibleModsWarning {

    @Redirect(
        method = "clientSetup",
        at = @At(value = "INVOKE", target = "Ljava/util/Map;getOrDefault(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", remap = false),
        require = 0,
        remap = false
    )
    private Object ssrcamerafixes$filterIncompatibleMods(Map<Object, Object> properties, Object key, Object fallback) {
        Object result = properties.getOrDefault(key, fallback);
        if (!"incompatibleMods".equals(key) || !(result instanceof List<?> list)) return result;
        List<Object> ids = new ArrayList<>(list);
        ids.removeIf(id -> "betterthirdperson".equals(String.valueOf(id)) || "valkyrienskies".equals(String.valueOf(id)));
        return ids;
    }
}
