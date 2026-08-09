package com.ssrcamerafixes.fabric.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Arrow velocity comes from getViewVector, which LivingEntity resolves off yHeadRot, but the rot packet
// only carries yRot, so the server would shoot along whatever head yaw it had before the crosshair snap.
@Mixin(CrossbowItem.class)
public abstract class MixinCrossbowServerHeadYaw {

    @Inject(method = "performShooting", at = @At("HEAD"))
    private static void ssrcamerafixes$alignHeadToShootYaw(Level level, LivingEntity shooter, InteractionHand hand,
                                                           ItemStack stack, float velocity, float inaccuracy,
                                                           CallbackInfo ci) {
        if (level.isClientSide || !(shooter instanceof Player)) return;
        shooter.yHeadRot = shooter.getYRot();
    }
}
