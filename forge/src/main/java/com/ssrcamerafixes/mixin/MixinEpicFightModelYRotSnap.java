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
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

// Mover skills (Phantom Ascent etc.) set model facing via setModelYRot. Do not override setModelYRot on
// sendPacket=true: dodge and charged attacks already ship the correct angle from EF and we were breaking them.
@Pseudo
@Mixin(targets = "yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch", remap = false)
public abstract class MixinEpicFightModelYRotSnap {

    @Shadow protected float modelYRot;
    @Shadow protected boolean useModelYRot;

    @Inject(method = "tick", at = @At("TAIL"), require = 0, remap = false)
    private void ssrcamerafixes$updateMoverFacingMidAir(CallbackInfo ci) {
        if (!this.useModelYRot) return;
        if (!ShoulderSurfingHelper.isCameraDecoupled()) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || player.onGround()) return;

        PlayerPatch<?> patch = EpicFightCapabilities.getEntityPatch(player, PlayerPatch.class);
        if (patch != null && patch.getEntityState().turningLocked()) return;

        Float facing = ssrcamerafixes$moverFacingFromInput();
        if (facing != null) {
            this.modelYRot = facing;
        }
    }

    private static Float ssrcamerafixes$moverFacingFromInput() {
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
