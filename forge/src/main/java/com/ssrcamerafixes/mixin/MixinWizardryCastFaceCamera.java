package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.compat.WizardryReduxHelper;
import com.ssrcamerafixes.handler.AimingFaceCameraHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Wizardry casts ride the vanilla use path with no rotation of their own, so the server aims them from the last
// synced yaw. useItem sends a Rot packet right after this, so writing the crosshair rotation here covers the
// instant spells that finish inside the use call as well as bombs.
@Mixin(MultiPlayerGameMode.class)
public abstract class MixinWizardryCastFaceCamera {

    @Inject(method = "useItem", at = @At("HEAD"))
    private void ssrcamerafixes$faceCrosshairForWizardryCast(Player player, InteractionHand hand,
                                                             CallbackInfoReturnable<?> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (player != mc.player) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;
        if (!WizardryReduxHelper.isCastItem(player.getItemInHand(hand).getItem())) return;

        AimingFaceCameraHandler.faceCrosshair(mc.player);
    }
}
