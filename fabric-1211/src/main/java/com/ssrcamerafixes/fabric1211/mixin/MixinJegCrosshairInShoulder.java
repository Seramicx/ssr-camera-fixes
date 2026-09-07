package com.ssrcamerafixes.fabric1211.mixin;

import com.ssrcamerafixes.compat.JegHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import net.minecraft.world.item.ItemStack;
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

    // Scoped guns get no crosshair at all on this port, an extra bail the Forge build and the NeoForge port
    // both lack. In shoulder view the scope overlay never takes over, so the crosshair has to stay
    @Redirect(method = "render",
            at = @At(value = "INVOKE", target = "Lttv/migami/jeg/gun/GunScopeSupport;hasTelescopicSight"),
            require = 0, remap = false)
    private static boolean ssrcamerafixes$scopedCrosshairInShoulderView(ItemStack stack) {
        return !ShoulderSurfingHelper.isShoulderSurfingActive() && JegHelper.hasTelescopicSight(stack);
    }
}
