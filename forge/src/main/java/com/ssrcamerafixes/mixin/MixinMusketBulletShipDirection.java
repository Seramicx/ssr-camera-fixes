package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ValkyrienSkiesHelper;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// GunItem.use builds the bullet velocity straight off the shooter angles, which on a ship are ship-local.
@Pseudo
@Mixin(targets = "ewewukek.musketmod.GunItem", remap = false)
public abstract class MixinMusketBulletShipDirection {

    @Redirect(
            method = {"m_7203_", "use"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getXRot()F", remap = true),
            require = 0
    )
    private float ssrcamerafixes$worldPitch(Player player) {
        float[] world = worldRot(player);
        return world != null ? world[1] : player.getXRot();
    }

    @Redirect(
            method = {"m_7203_", "use"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getYRot()F", remap = true),
            require = 0
    )
    private float ssrcamerafixes$worldYaw(Player player) {
        float[] world = worldRot(player);
        return world != null ? world[0] : player.getYRot();
    }

    private static float[] worldRot(Player player) {
        if (player == null || !ValkyrienSkiesHelper.isMountedOnShip(player)) return null;
        return ValkyrienSkiesHelper.shipLocalRotToWorldRot(player, player.getYRot(), player.getXRot());
    }
}
