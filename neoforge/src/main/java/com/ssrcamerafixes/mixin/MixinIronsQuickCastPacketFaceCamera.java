package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.IronSpellsHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Quick-cast keybind sends QuickCastPacket(slot) from a client tick with no rotation. The int constructor is
// the client send (the FriendlyByteBuf one is server decode), so face the crosshair before the packet leaves
@Pseudo
@Mixin(targets = "io.redspace.ironsspellbooks.network.casting.QuickCastPacket", remap = false)
public abstract class MixinIronsQuickCastPacketFaceCamera {

    @Inject(method = "<init>(I)V", at = @At("TAIL"), require = 0, remap = false)
    private void ssrcamerafixes$faceCrosshairForQuickCast(CallbackInfo ci) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;

        IronSpellsHelper.signalCast();
        ShoulderSurfingHelper.lookAtCrosshairTarget();
    }
}
