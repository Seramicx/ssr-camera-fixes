package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ValkyrienSkiesHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// CGM builds aim from shooter yaw/pitch (ship-local on VS) then uses it as a world-space velocity.
// Rotate ship→world so shots stay on the crosshair after the ship turns.
@Pseudo
@Mixin(targets = "com.mrcrayfish.guns.entity.ProjectileEntity", remap = false)
public abstract class MixinCgmProjectileShipDirection {

    @Inject(method = "getDirection", at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private void ssrcamerafixes$shipToWorldAim(LivingEntity shooter, ItemStack weapon, @Coerce Object item,
                                               @Coerce Object modifiedGun, CallbackInfoReturnable<Vec3> cir) {
        Vec3 local = cir.getReturnValue();
        if (local == null || shooter == null) return;
        if (!ValkyrienSkiesHelper.isMountedOnShip(shooter)) return;

        Vec3 world = ValkyrienSkiesHelper.shipToWorldDirection(shooter, local);
        if (world != null) {
            cir.setReturnValue(world);
        }
    }
}
