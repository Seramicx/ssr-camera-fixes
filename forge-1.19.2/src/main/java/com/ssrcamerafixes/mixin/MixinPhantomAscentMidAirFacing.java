package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Re-aim from the render path (getModelMatrix), not a tick handler, so mid-air re-aiming never disturbs the
// movement/launch yaw.
@Pseudo
@Mixin(targets = "yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch", remap = false)
public abstract class MixinPhantomAscentMidAirFacing {

    @Shadow protected float bodyYaw;

    @Inject(method = "getModelMatrix", at = @At("HEAD"), require = 0, remap = false)
    private void ssrcamerafixes$reaimRoll(float partialTick, CallbackInfoReturnable<?> cir) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.isOnGround()) return;
        if (!ShoulderSurfingHelper.isCameraDecoupled()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;
        if (!EpicFightHelper.animationOwnsLivingMotion(player)) return;
        if (EpicFightHelper.isWallClimbing(player)) return;

        int forward = (mc.options.keyUp.isDown() ? 1 : 0) + (mc.options.keyDown.isDown() ? -1 : 0);
        int horizon = (mc.options.keyLeft.isDown() ? 1 : 0) + (mc.options.keyRight.isDown() ? -1 : 0);
        if (forward == 0 && horizon == 0) return;

        float degree = -(90F * horizon * (1 - Math.abs(forward)) + 45F * forward * horizon);
        float facing = Mth.wrapDegrees(ShoulderSurfingHelper.getCameraYaw() + (forward < 0 ? 180F : 0F) + degree);
        // modelYRot lives on the parent PlayerPatch (not shadowable through @Pseudo), so write it via reflection.
        EpicFightHelper.setModelYRot(player, Mth.wrapDegrees(facing - this.bodyYaw));
    }
}
