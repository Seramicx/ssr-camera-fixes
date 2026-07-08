package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// 1.19.2 plays the roll via playAnimationClientPreemptive (raw StaticAnimation), not playAnimationInClientSide
@Pseudo
@Mixin(targets = "yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch", remap = false)
public abstract class MixinPhantomAscentForwardRoll {

    @ModifyVariable(method = "playAnimationClientPreemptive", at = @At("HEAD"), argsOnly = true, require = 0, remap = false)
    private StaticAnimation ssrcamerafixes$forwardRoll(StaticAnimation animation) {
        if (!ShoulderSurfingHelper.isCameraDecoupled()) return animation;
        if (animation == Animations.BIPED_PHANTOM_ASCENT_BACKWARD) {
            return Animations.BIPED_PHANTOM_ASCENT_FORWARD;
        }
        return animation;
    }
}
