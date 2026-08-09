package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.compat.ValkyrienSkiesHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// VS third-person on a ship raycasts getMaxZoomIgnoringMountedShip in world space. Turning the camera
// (especially with an SSR shoulder) hits different ship geometry each frame → distance/shoulder jitter
// that depends on turn direction. Keep full zoom; SSR (+ our LevelRenderer shoulder) owns the offset.
@Pseudo
@Mixin(targets = "org.valkyrienskies.mod.mixin.client.MixinCamera", remap = false)
public abstract class MixinVsCameraSkipZoomOnSsr {

    @Inject(
            method = "getMaxZoomIgnoringMountedShip",
            at = @At("HEAD"),
            cancellable = true,
            require = 0,
            remap = false
    )
    private void ssrcamerafixes$skipShipZoomRaycast(Level level, double maxDist, @Coerce Object ship,
                                                    CallbackInfoReturnable<Double> cir) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!ValkyrienSkiesHelper.isMountedOnShip(player)) return;
        cir.setReturnValue(maxDist);
    }
}
