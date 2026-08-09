package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ValkyrienSkiesHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Guns with firesArrows spawn a vanilla Arrow from ServerPlayHandler instead of going through
// ProjectileEntity, so MixinScorchedProjectileShipDirection never sees them. Same ship-local
// yaw/pitch used as a world velocity. The eye position is already world, the 0.5 spawn nudge is not.
@Pseudo
@Mixin(targets = "top.ribs.scguns.common.network.ServerPlayHandler", remap = false)
public abstract class MixinScorchedArrowShipDirection {

    @Inject(method = "getArrow", at = @At("RETURN"), require = 0, remap = false)
    private static void ssrcamerafixes$shipToWorldArrow(ServerPlayer player, Level world, ItemStack heldItem,
                                                        @Coerce Object modifiedGun,
                                                        CallbackInfoReturnable<Arrow> cir) {
        Arrow arrow = cir.getReturnValue();
        if (arrow == null || player == null) return;
        if (!ValkyrienSkiesHelper.isMountedOnShip(player)) return;

        Vec3 worldVel = ValkyrienSkiesHelper.shipToWorldDirection(player, arrow.getDeltaMovement());
        if (worldVel == null) return;

        arrow.setDeltaMovement(worldVel);

        Vec3 dir = worldVel.normalize();
        arrow.moveTo(player.getEyePosition().add(dir.x * 0.5, -0.1, dir.z * 0.5));

        double horiz = Math.sqrt(worldVel.x * worldVel.x + worldVel.z * worldVel.z);
        arrow.setYRot((float) (Mth.atan2(worldVel.x, worldVel.z) * Mth.RAD_TO_DEG));
        arrow.setXRot((float) (Mth.atan2(worldVel.y, horiz) * Mth.RAD_TO_DEG));
        arrow.yRotO = arrow.getYRot();
        arrow.xRotO = arrow.getXRot();
    }
}
