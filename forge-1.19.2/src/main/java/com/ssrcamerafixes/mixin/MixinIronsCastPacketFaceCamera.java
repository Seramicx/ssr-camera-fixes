package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Right-click Firebolt sends ServerboundCast from a client tick with no rotation; the server casts from the
// synced yaw. The no-arg constructor is the client send, so face the crosshair before the packet leaves
@Pseudo
@Mixin(targets = "io.redspace.ironsspellbooks.network.ServerboundCast", remap = false)
public abstract class MixinIronsCastPacketFaceCamera {

    @Inject(method = "<init>()V", at = @At("TAIL"), require = 0, remap = false)
    private void ssrcamerafixes$faceCrosshairForCast(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;

        EpicFightHelper.signalCast();
        ShoulderSurfingHelper.lookAtCrosshairTarget();
    }
}
