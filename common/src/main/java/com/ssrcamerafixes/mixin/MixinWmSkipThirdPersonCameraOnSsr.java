package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Weapon Master rebuilds position and rotation from the player's own look at Camera.setup TAIL whenever a
// sweeper weapon is held in third person, so it throws away the decoupled camera SSR just placed: the view
// snaps back to the body and movement no longer matches what is on screen.
@Pseudo
@Mixin(targets = "com.sky.weaponmaster.mixins.CameraMixin", remap = false)
public abstract class MixinWmSkipThirdPersonCameraOnSsr {
    @Inject(
            method = "setup",
            at = @At("HEAD"),
            cancellable = true,
            require = 0,
            remap = false
    )
    private void ssrcamerafixes$skipSweeperCamera(BlockGetter area, Entity entity, boolean detached,
                                                  boolean flipped, float partialTick, CallbackInfo wmCi,
                                                  CallbackInfo ci) {
        if (ShoulderSurfingHelper.isShoulderSurfingActive()) {
            ci.cancel();
        }
    }
}
