package com.ssrcamerafixes.fabric.mixin;

import com.ssrcamerafixes.compat.SpellEngineTargetHelper;
import com.ssrcamerafixes.fabric.compat.WizardsHelper;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Pseudo
@Mixin(targets = "net.spell_engine.utils.TargetHelper", remap = false)
public abstract class MixinSpellEngineMountedTargetRay {

    @Inject(method = "targetFromRaycast", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
    private static void ssrcamerafixes$mountedCrosshairRay(
            Entity caster, float range, Predicate<Entity> predicate, CallbackInfoReturnable<Entity> cir) {
        if (!SpellEngineTargetHelper.shouldUseMountedCameraRay(caster)) return;
        if (!WizardsHelper.isInstantCasting()) return;
        cir.setReturnValue(SpellEngineTargetHelper.targetFromMountedCameraRay(caster, range, predicate));
    }
}
