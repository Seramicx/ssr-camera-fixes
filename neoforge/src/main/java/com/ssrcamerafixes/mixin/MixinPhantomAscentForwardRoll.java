package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.gameasset.Animations;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// Target LocalPlayerPatch: its playAnimationInClientSide override never calls super, so a base-class mixin won't run
@Pseudo
@Mixin(targets = "yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch", remap = false)
public abstract class MixinPhantomAscentForwardRoll {

    @ModifyVariable(method = "playAnimationInClientSide", at = @At("HEAD"), argsOnly = true, require = 0, remap = false)
    private AssetAccessor<?> ssrcamerafixes$forwardRoll(AssetAccessor<?> animation) {
        if (!ShoulderSurfingHelper.isCameraDecoupled()) return animation;
        if (animation == Animations.BIPED_PHANTOM_ASCENT_BACKWARD) {
            return Animations.BIPED_PHANTOM_ASCENT_FORWARD;
        }
        return animation;
    }
}
