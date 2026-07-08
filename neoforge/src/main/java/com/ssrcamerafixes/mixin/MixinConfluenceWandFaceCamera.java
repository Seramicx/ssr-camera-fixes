package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ConfluenceHelper;
import com.ssrcamerafixes.compat.EpicFightHelper;
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

// Confluence mana weapons spawn their projectile server-side from the player's body rotation the instant the
// use packet lands, and that packet carries the rotation captured here at HEAD. Facing the crosshair before it
// is built is the only point early enough; a tick handler loses the race and the shot leaves at the old yaw.
@Mixin(MultiPlayerGameMode.class)
public abstract class MixinConfluenceWandFaceCamera {

    @Inject(method = "useItem", at = @At("HEAD"))
    private void ssrcamerafixes$faceCrosshairForConfluenceWand(Player player, InteractionHand hand,
                                                               CallbackInfoReturnable<?> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (player != mc.player) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;
        if (!ConfluenceHelper.isHoldingManaWeapon(mc.player)) return;

        ShoulderSurfingHelper.lookAtCrosshairTarget();
    }
}
