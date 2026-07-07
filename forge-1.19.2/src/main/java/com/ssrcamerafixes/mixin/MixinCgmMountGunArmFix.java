package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.CgmGunAimHelper;
import com.ssrcamerafixes.compat.GunModHelper;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// CGM injects into setupAnim and skips WeaponPose when isLocalPlayer() && limbSwing==0. Redirect that check on
// PlayerModel (not CGM's mixin class — targeting another mixin crashes at launch).
@Mixin(PlayerModel.class)
public abstract class MixinCgmMountGunArmFix<T extends LivingEntity> {

    @Redirect(
            method = "setupAnim*",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isLocalPlayer()Z"),
            require = 0
    )
    private boolean ssrcamerafixes$runWeaponPoseOnMount(Player player) {
        if (GunModHelper.isHoldingGun(player) && CgmGunAimHelper.needsMountedShotFix(player)) {
            return false;
        }
        return player.isLocalPlayer();
    }
}
