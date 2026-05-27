package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.CombatRollHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MixinCombatRollDirection {

    private static float ssrcamerafixes$savedYRot = Float.NaN;

    @Inject(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;handleKeybinds()V"
        )
    )
    private void ssrcamerafixes$beforeHandleKeybinds(CallbackInfo ci) {
        if (!CombatRollHelper.isLoaded()) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;

        LocalPlayer player = ((Minecraft) (Object) this).player;
        if (player == null) return;

        ssrcamerafixes$savedYRot = player.getYRot();
        player.setYRot(player.yBodyRot);
    }

    @Inject(
        method = "tick",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/Minecraft;handleKeybinds()V",
            shift = At.Shift.AFTER
        )
    )
    private void ssrcamerafixes$afterHandleKeybinds(CallbackInfo ci) {
        float saved = ssrcamerafixes$savedYRot;
        if (Float.isNaN(saved)) return;
        ssrcamerafixes$savedYRot = Float.NaN;

        LocalPlayer player = ((Minecraft) (Object) this).player;
        if (player != null) {
            player.setYRot(saved);
        }
    }
}
