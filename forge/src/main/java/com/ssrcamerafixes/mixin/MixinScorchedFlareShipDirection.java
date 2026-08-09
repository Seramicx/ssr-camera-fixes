package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ValkyrienSkiesHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// The flare velocity comes from getLookAngle, which is ship-local on a mount. A @Redirect on that call needs
// a vanilla member the refmap will not bind inside a @Pseudo target, so rotate to world across the call.
@Pseudo
@Mixin(targets = "top.ribs.scguns.item.FlarePistolItem", remap = false)
public abstract class MixinScorchedFlareShipDirection {

    private float ssrcamerafixes$savedYRot;
    private float ssrcamerafixes$savedXRot;
    private boolean ssrcamerafixes$swapped;

    @Inject(method = {"m_7203_", "use"}, at = @At("HEAD"), require = 0, remap = false)
    private void ssrcamerafixes$toWorldRot(Level level, Player player, InteractionHand hand,
                                           CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ssrcamerafixes$swapped = false;
        if (player == null || !ValkyrienSkiesHelper.isMountedOnShip(player)) return;

        float[] world = ValkyrienSkiesHelper.shipLocalRotToWorldRot(player, player.getYRot(), player.getXRot());
        if (world == null) return;

        ssrcamerafixes$savedYRot = player.getYRot();
        ssrcamerafixes$savedXRot = player.getXRot();
        ssrcamerafixes$swapped = true;
        player.setYRot(world[0]);
        player.setXRot(world[1]);
    }

    @Inject(method = {"m_7203_", "use"}, at = @At("RETURN"), require = 0, remap = false)
    private void ssrcamerafixes$restoreRot(Level level, Player player, InteractionHand hand,
                                           CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        if (!ssrcamerafixes$swapped || player == null) return;
        ssrcamerafixes$swapped = false;
        player.setYRot(ssrcamerafixes$savedYRot);
        player.setXRot(ssrcamerafixes$savedXRot);
    }
}
