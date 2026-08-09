package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.handler.AimingFaceCameraHandler;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// ClientMessagePlayerShoot carries no rotation; server shoots from the player's synced look. This ctor is
// also used on the server decode path, so gate on Dist.CLIENT.
@Pseudo
@Mixin(targets = "com.tacz.guns.network.message.ClientMessagePlayerShoot", remap = false)
public abstract class MixinTaczShootFaceCamera {

    @Inject(method = "<init>(JF)V", at = @At("TAIL"), require = 0, remap = false)
    private void ssrcamerafixes$faceCrosshairForShoot(long timestamp, float chargeProgress, CallbackInfo ci) {
        if (FMLEnvironment.dist != Dist.CLIENT) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;

        AimingFaceCameraHandler.faceCrosshairAndSync(mc, player);
    }
}
