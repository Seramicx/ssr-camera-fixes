package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

// The 1.21.1 port cancels the vanilla crosshair whenever a gun is held and then draws its own in first person only,
// with no Shoulder Surfing check at all, so third person is left with no crosshair. Upstream also skips the crosshair
// past half aim progress, but only in first person, and the port dropped that half of the condition
@Pseudo
@Mixin(targets = "ttv.migami.jeg.client.CrosshairHandler", remap = false)
public abstract class MixinJegCrosshairInShoulder {

    @Redirect(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;getCameraType()Lnet/minecraft/client/CameraType;", remap = true),
            require = 0, remap = false)
    private static CameraType ssrcamerafixes$crosshairInShoulderView(Options options) {
        if (ShoulderSurfingHelper.isShoulderSurfingActive()) {
            return CameraType.FIRST_PERSON;
        }
        return options.getCameraType();
    }

    // Normalised aim progress never exceeds 1, so this puts the aiming bail out of reach
    @ModifyConstant(method = "render", constant = @Constant(floatValue = 0.5F), require = 0, remap = false)
    private static float ssrcamerafixes$adsThresholdInShoulderView(float threshold) {
        return ShoulderSurfingHelper.isShoulderSurfingActive() ? 2.0F : threshold;
    }

    // Ordinal 0 is the bolt action rifle check, which drops the crosshair entirely. The scope overlay never takes
    // over in shoulder view, so that leaves the rifle with nothing to aim by. Later ordinals pick the crosshair style
    @Redirect(method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/resources/ResourceLocation;equals(Ljava/lang/Object;)Z", ordinal = 0, remap = true),
            require = 0, remap = false)
    private static boolean ssrcamerafixes$scopedCrosshairInShoulderView(ResourceLocation id, Object other) {
        return !ShoulderSurfingHelper.isShoulderSurfingActive() && id.equals(other);
    }
}
