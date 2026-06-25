package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.SableHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// On a Sable sub-level the 400-block dynamic-crosshair reach makes SSR build an entity-pick AABB that Sable's
// sub-level entity getter rejects as abnormally large (log spam, and a crash on older Sable). Clamp the reach
// to a normal range while seated on a contraption so the AABB stays sane.
@Pseudo
@Mixin(targets = "com.github.exopandora.shouldersurfing.client.ObjectPicker", remap = false)
public abstract class MixinSsrObjectPickerSubLevel {

    @ModifyVariable(method = "pickEntities", at = @At("HEAD"), argsOnly = true, ordinal = 0, require = 0, remap = false)
    private double ssrcamerafixes$clampReachOnSubLevel(double interactionRange) {
        if (interactionRange <= 16.0D) return interactionRange;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null && SableHelper.isOnSubLevel(player)) {
            return 16.0D;
        }
        return interactionRange;
    }
}
