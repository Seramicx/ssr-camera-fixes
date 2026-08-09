package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ValkyrienSkiesHelper;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Each item throws a different entity type, so redirecting shootFromRotation would need one descriptor per
// target. Rewriting the angle reads covers them all.
@Pseudo
@Mixin(targets = {
        "top.ribs.scguns.item.GrenadeItem",
        "top.ribs.scguns.item.ChokeBombItem",
        "top.ribs.scguns.item.GasGrenadeItem",
        "top.ribs.scguns.item.HellfireBombItem",
        "top.ribs.scguns.item.MolotovCocktailItem",
        "top.ribs.scguns.item.NailBombItem",
        "top.ribs.scguns.item.SwarmBombItem",
        "top.ribs.scguns.item.BeaconGrenadeItem",
        "top.ribs.scguns.item.ThrowableShotballItem"
}, remap = false)
public abstract class MixinScorchedThrowableShipDirection {

    @Redirect(
            method = {"m_5551_", "m_5922_", "releaseUsing", "finishUsingItem"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getXRot()F", remap = true),
            require = 0
    )
    private float ssrcamerafixes$worldPitch(LivingEntity thrower) {
        float[] world = worldRot(thrower);
        return world != null ? world[1] : thrower.getXRot();
    }

    @Redirect(
            method = {"m_5551_", "m_5922_", "releaseUsing", "finishUsingItem"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getYRot()F", remap = true),
            require = 0
    )
    private float ssrcamerafixes$worldYaw(LivingEntity thrower) {
        float[] world = worldRot(thrower);
        return world != null ? world[0] : thrower.getYRot();
    }

    private static float[] worldRot(LivingEntity thrower) {
        if (thrower == null || !ValkyrienSkiesHelper.isMountedOnShip(thrower)) return null;
        return ValkyrienSkiesHelper.shipLocalRotToWorldRot(thrower, thrower.getYRot(), thrower.getXRot());
    }
}
