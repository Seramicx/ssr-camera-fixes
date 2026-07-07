package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.CgmGunRenderHelper;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// CGM PlayerModelMixin is priority 1000; run after it so riding arm-zeroing does not win.
@Mixin(value = PlayerModel.class, priority = 1100)
public abstract class MixinCgmMountedGunArms<T extends LivingEntity> {

    @Inject(method = "setupAnim*", at = @At("TAIL"))
    private void ssrcamerafixes$mountedGunArms(
            T entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci) {
        if (!(entity instanceof LocalPlayer player)) return;
        CgmGunRenderHelper.applyMountedWeaponArms(player, (PlayerModel<?>) (Object) this, limbSwing);
    }
}
