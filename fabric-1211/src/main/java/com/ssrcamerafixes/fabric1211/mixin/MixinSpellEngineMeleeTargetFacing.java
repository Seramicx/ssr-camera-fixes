package com.ssrcamerafixes.fabric1211.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "net.spell_engine.internals.melee.TargetFinder", remap = false)
public abstract class MixinSpellEngineMeleeTargetFacing {

    @Redirect(
            method = "findAttackTargetResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getYRot()F",
                    remap = true
            ),
            require = 0,
            remap = false
    )
    private static float ssrcamerafixes$meleeObbYaw(Player player) {
        if (ShoulderSurfingHelper.isCameraDecoupled()) {
            return ShoulderSurfingHelper.getCameraYaw();
        }
        return player.getYRot();
    }

    @Redirect(
            method = "findAttackTargetResult",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Player;getXRot()F",
                    remap = true
            ),
            require = 0,
            remap = false
    )
    private static float ssrcamerafixes$meleeObbPitch(Player player) {
        if (ShoulderSurfingHelper.isCameraDecoupled()) {
            return ShoulderSurfingHelper.getCameraXRot();
        }
        return player.getXRot();
    }
}
