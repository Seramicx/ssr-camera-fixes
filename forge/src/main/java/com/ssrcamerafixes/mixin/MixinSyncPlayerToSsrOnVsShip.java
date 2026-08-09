package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.BetterMountSteeringHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.compat.ValkyrienSkiesHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// On a VS ship the camera basis is read from player.yRot, so without syncing the player to SSR's camera
// the view stays stuck on the body's last facing instead of following the mouse.
// Skip while BMS mount-rotate / decouple is active — forcing camera yaw onto the player every setup
// fights the ship turn and jitters SSR offset (worse on v5 + VS camera rebase).
@Mixin(Camera.class)
public abstract class MixinSyncPlayerToSsrOnVsShip {

    @Inject(method = "setup", at = @At("HEAD"))
    private void ssrcamerafixes$syncPlayerRotationToSsr(BlockGetter level, Entity entity,
                                                        boolean detached, boolean thirdPersonReverse,
                                                        float partialTick, CallbackInfo ci) {
        if (!detached) return;
        if (!(entity instanceof LocalPlayer player)) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!ValkyrienSkiesHelper.isMountedOnShip(player)) return;
        if (BetterMountSteeringHelper.isMountRotateActive()
                || BetterMountSteeringHelper.isDecoupleTransitioning()) {
            return;
        }

        float ssrYaw = ShoulderSurfingHelper.getCameraYaw();
        float ssrPitch = ShoulderSurfingHelper.getCameraXRot();
        if (Float.isNaN(ssrYaw) || Float.isNaN(ssrPitch)) return;

        player.setYRot(ssrYaw);
        player.setXRot(ssrPitch);
        player.yRotO = ssrYaw;
        player.xRotO = ssrPitch;
        player.yBodyRot = ssrYaw;
        player.yBodyRotO = ssrYaw;
        player.yHeadRot = ssrYaw;
        player.yHeadRotO = ssrYaw;
    }

    // SSR places the shoulder using its smoothed render rotation while VS rebuilds the camera basis from the player,
    // so hand the render rotation back before VS runs or the two bases disagree mid-turn and the offset swings
    @Inject(method = "setup", at = @At("RETURN"))
    private void ssrcamerafixes$syncPlayerToRenderRotation(BlockGetter level, Entity entity,
                                                           boolean detached, boolean thirdPersonReverse,
                                                           float partialTick, CallbackInfo ci) {
        if (!detached) return;
        if (!(entity instanceof LocalPlayer player)) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!ValkyrienSkiesHelper.isMountedOnShip(player)) return;

        Camera self = (Camera) (Object) this;
        float renderYaw = self.getYRot();
        float renderPitch = self.getXRot();

        player.setYRot(renderYaw);
        player.setXRot(renderPitch);
        player.yRotO = renderYaw;
        player.xRotO = renderPitch;
        player.yBodyRot = renderYaw;
        player.yBodyRotO = renderYaw;
        player.yHeadRot = renderYaw;
        player.yHeadRotO = renderYaw;
    }
}
