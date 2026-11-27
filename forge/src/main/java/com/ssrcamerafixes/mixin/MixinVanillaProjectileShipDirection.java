package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ValkyrienSkiesHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// A rider on a VS ship keeps ship-local rotation, so bows, tridents and thrown items left in the ship's frame
@Mixin(Projectile.class)
public abstract class MixinVanillaProjectileShipDirection {

    @Unique
    private static final ThreadLocal<Boolean> ssrcamerafixes$converting = ThreadLocal.withInitial(() -> false);

    @Inject(method = "shootFromRotation", at = @At("HEAD"), cancellable = true, require = 0)
    private void ssrcamerafixes$shipToWorld(Entity shooter, float xRot, float yRot, float roll, float velocity, float inaccuracy, CallbackInfo ci) {
        if (shooter == null || shooter.level().isClientSide() || ssrcamerafixes$converting.get()) return;
        // only the raw ship-local look, callers that already converted pass something else
        if (Math.abs(Mth.wrapDegrees(yRot - shooter.getYRot())) > 0.01F || Math.abs(xRot - shooter.getXRot()) > 0.01F) return;
        if (!ValkyrienSkiesHelper.isMountedOnShip(shooter)) return;
        float[] world = ValkyrienSkiesHelper.shipLocalRotToWorldRot(shooter, yRot, xRot);
        if (world == null) return;

        ci.cancel();
        ssrcamerafixes$converting.set(true);
        try {
            ((Projectile) (Object) this).shootFromRotation(shooter, world[1], world[0], roll, velocity, inaccuracy);
        } finally {
            ssrcamerafixes$converting.set(false);
        }
    }
}
