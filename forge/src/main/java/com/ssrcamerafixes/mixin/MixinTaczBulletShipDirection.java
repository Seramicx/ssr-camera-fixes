package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ValkyrienSkiesHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TACZ doBulletSpread either calls EntityKineticBullet.shootFromRotation(...Vector2d) or vanilla
// Projectile.shootFromRotation (m_37251_). Both build velocity from ship-local pitch/yaw as if it
// were world. Hook RETURN here so both paths get ship→world.
@Pseudo
@Mixin(targets = "com.tacz.guns.item.ModernKineticGunItem", remap = false)
public abstract class MixinTaczBulletShipDirection {

    @Inject(
            method = "doBulletSpread(Lcom/tacz/guns/entity/shooter/ShooterDataHolder;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/projectile/Projectile;IFFFF)V",
            at = @At("RETURN"),
            require = 0,
            remap = false
    )
    private void ssrcamerafixes$shipToWorldAim(@Coerce Object dataHolder, ItemStack stack, LivingEntity shooter,
                                               Projectile bullet, int bulletIndex, float speed,
                                               float inaccuracy, float pitch, float yaw, CallbackInfo ci) {
        if (shooter == null || bullet == null) return;
        if (!ValkyrienSkiesHelper.isMountedOnShip(shooter)) return;

        Vec3 local = bullet.getDeltaMovement();
        Vec3 world = ValkyrienSkiesHelper.shipToWorldDirection(shooter, local);
        if (world != null) {
            bullet.setDeltaMovement(world);
        }
    }
}
