package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.IronSpellsHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Instant casts (Firebolt right-click) resolve the same tick the use packet lands and never set isCasting, so a
// tick handler aims too late. Face the crosshair at useItem HEAD before the packet is built; flag the mount
// steerer to yield this tick so it doesn't reassert the body yaw over the cast.
@Mixin(MultiPlayerGameMode.class)
public abstract class MixinIronsInstantCastFaceCamera {

    @Inject(method = "useItem", at = @At("HEAD"))
    private void ssrcamerafixes$faceCrosshairForInstantCast(Player player, InteractionHand hand,
                                                            CallbackInfoReturnable<?> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (player != mc.player) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;
        if (!IronSpellsHelper.isIronsItem(player.getItemInHand(hand).getItem())) return;

        // Instant casts resolve server-side over the next few ticks; latch so the aim handlers keep correcting
        // the rotation against Better Mount Steering's end-of-tick reassert until the cast lands
        IronSpellsHelper.signalCast();
        ShoulderSurfingHelper.lookAtCrosshairTarget();
    }
}
