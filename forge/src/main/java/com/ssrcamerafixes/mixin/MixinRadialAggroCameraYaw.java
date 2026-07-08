package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// The aggro arrow rotates the target offset by the player's body yaw, but under a decoupled camera the
// on-screen view is the camera yaw, so the arrow points relative to the body instead of where you look
@Pseudo
@Mixin(targets = "com.mrbysco.radialaggroindicator.client.HudHandler", remap = false)
public abstract class MixinRadialAggroCameraYaw {

    @Redirect(
        method = "calculateScreenAngle",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getYRot()F", remap = true),
        require = 0, remap = false
    )
    private static float ssrcamerafixes$useCameraYaw(Player player) {
        if (ShoulderSurfingHelper.isShoulderSurfingActive()) {
            return ShoulderSurfingHelper.getCameraYaw();
        }
        return player.getYRot();
    }
}
