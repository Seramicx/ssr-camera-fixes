package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Immersive Aircraft pulls the camera back by the aircraft's own zoom at Camera.setup TAIL, which stacks on
// top of the shoulder offset SSR already applied, so the camera ends up far behind the plane. Drop their
// pull-back while SSR is driving and let it own the distance like it does for every other mount.
@Pseudo
@Mixin(targets = "immersive_aircraft.mixin.client.CameraMixin", remap = false)
public abstract class MixinIaCameraSkipZoomOnSsr {
    @Inject(
            method = "ia$setup",
            at = @At("HEAD"),
            cancellable = true,
            require = 0,
            remap = false
    )
    private void ssrcamerafixes$skipAircraftZoom(BlockGetter area, Entity entity, boolean thirdPerson,
                                                 boolean inverseView, float tickDelta, CallbackInfo iaCi,
                                                 CallbackInfo ci) {
        if (ShoulderSurfingHelper.isShoulderSurfingActive()) {
            ci.cancel();
        }
    }
}
