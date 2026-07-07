package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.CgmGunAimHelper;
import com.ssrcamerafixes.compat.GunModHelper;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.mrcrayfish.guns.client.render.pose.WeaponPose", remap = false)
public abstract class MixinCgmWeaponPosePitch {

    @Inject(method = "getPlayerPitch", at = @At("RETURN"), cancellable = true, require = 0, remap = false)
    private void ssrcamerafixes$mountedGunPitch(Player player, CallbackInfoReturnable<Float> cir) {
        if (!CgmGunAimHelper.needsMountedShotFix(player)) return;
        if (!GunModHelper.isHoldingGun(player)) return;
        cir.setReturnValue(CgmGunAimHelper.shotPitch() / 90F);
    }
}
