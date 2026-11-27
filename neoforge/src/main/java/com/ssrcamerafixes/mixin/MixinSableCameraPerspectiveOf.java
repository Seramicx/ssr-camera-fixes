package com.ssrcamerafixes.mixin;

import com.github.exopandora.shouldersurfing.api.client.Perspective;
import net.minecraft.client.CameraType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Perspective.class, remap = false)
public abstract class MixinSableCameraPerspectiveOf {

    @Inject(method = "of", at = @At("HEAD"), cancellable = true, require = 0)
    private static void ssrcamerafixes$sableViewIsThirdPerson(CameraType cameraType, boolean shoulderSurfing, CallbackInfoReturnable<Perspective> cir) {
        if (cameraType != CameraType.FIRST_PERSON && cameraType != CameraType.THIRD_PERSON_BACK && cameraType != CameraType.THIRD_PERSON_FRONT) {
            cir.setReturnValue(Perspective.THIRD_PERSON_BACK);
        }
    }
}
