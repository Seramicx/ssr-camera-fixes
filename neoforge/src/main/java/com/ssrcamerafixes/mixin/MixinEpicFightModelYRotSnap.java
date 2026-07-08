package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Mover skills (Phantom Ascent etc.) set the model facing via setModelYRot using an internal degree formula
// that, under a decoupled camera, faces the wrong way for strafe/diagonal input (e.g. left looks backward).
// Recompute the facing from the camera yaw and the actual movement keys, both when the skill sets it and on
// each tick the skill facing stays active, so the roll turns to match if input changes mid-air. Visual only.
@Pseudo
@Mixin(targets = "yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch", remap = false)
public abstract class MixinEpicFightModelYRotSnap {

    @Shadow protected float modelYRot;
    @Shadow protected float modelYRotO;
    @Shadow protected boolean useModelYRot;

    // sendPacket=true marks a deliberate skill facing-set; the per-tick turning-lock adjustments pass false.
    @Inject(method = "setModelYRot", at = @At("TAIL"), require = 0, remap = false)
    private void ssrcamerafixes$correctFacing(float rotDeg, boolean sendPacket, CallbackInfo ci) {
        if (!sendPacket) return;
        if (!ShoulderSurfingHelper.isCameraDecoupled()) return;
        Float facing = ssrcamerafixes$cameraRelativeFacing();
        if (facing != null) {
            this.modelYRot = facing;
        }
        this.modelYRotO = this.modelYRot;
    }

    // preTick sets modelYRotO to the prior modelYRot at HEAD, so writing modelYRot here lerps instead of snapping
    @Inject(method = "preTick", at = @At("TAIL"), require = 0, remap = false)
    private void ssrcamerafixes$updateFacingMidAir(CallbackInfo ci) {
        if (!this.useModelYRot) return;
        if (!ShoulderSurfingHelper.isCameraDecoupled()) return;
        Float facing = ssrcamerafixes$cameraRelativeFacing();
        if (facing != null) {
            this.modelYRot = facing;
        }
    }

    // Camera yaw rotated by the movement-key direction, or null when no direction is held.
    private static Float ssrcamerafixes$cameraRelativeFacing() {
        Minecraft mc = Minecraft.getInstance();
        Options options = mc.options;
        LocalPlayer player = mc.player;
        if (options == null || player == null) return null;
        int forward = (options.keyUp.isDown() ? 1 : 0) + (options.keyDown.isDown() ? -1 : 0);
        int strafeLeft = (options.keyLeft.isDown() ? 1 : 0) + (options.keyRight.isDown() ? -1 : 0);
        if (forward == 0 && strafeLeft == 0) return null;
        float offset = (float) -Math.toDegrees(Math.atan2(strafeLeft, forward));
        return Mth.wrapDegrees(ShoulderSurfingHelper.getCameraYaw() + offset);
    }
}
