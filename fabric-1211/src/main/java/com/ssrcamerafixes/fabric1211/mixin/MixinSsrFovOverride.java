package com.ssrcamerafixes.fabric1211.mixin;

import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.config.ICameraConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// SSR 5.x's fov_override writes into partialTick on 1.20.1/1.21.1, so the override never reaches the fov
@Mixin(GameRenderer.class)
public abstract class MixinSsrFovOverride {

    @Shadow private float oldFov;
    @Shadow private float fov;

    @Unique private float ssrcamerafixes$partialTick;
    @Unique private float ssrcamerafixes$ssrPartialTick;
    @Unique private boolean ssrcamerafixes$broken;

    @Inject(method = "getFov", at = @At("HEAD"), require = 0)
    private void ssrcamerafixes$savePartialTick(Camera camera, float partialTick, boolean useFovSetting, CallbackInfoReturnable<Double> cir) {
        ssrcamerafixes$partialTick = partialTick;
        ssrcamerafixes$broken = false;
    }

    @ModifyVariable(method = "getFov", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getEntity()Lnet/minecraft/world/entity/Entity;", ordinal = 0), ordinal = 0, argsOnly = true, require = 0)
    private float ssrcamerafixes$restorePartialTick(float partialTick) {
        if (partialTick == ssrcamerafixes$partialTick || !ssrcamerafixes$overrideActive()) return partialTick;
        ssrcamerafixes$broken = true;
        ssrcamerafixes$ssrPartialTick = partialTick;
        return ssrcamerafixes$partialTick;
    }

    @ModifyVariable(method = "getFov", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getFluidInCamera()Lnet/minecraft/world/level/material/FogType;", ordinal = 0), ordinal = 0, require = 0)
    private double ssrcamerafixes$applyOverride(double value) {
        if (!ssrcamerafixes$broken) return value;
        ssrcamerafixes$broken = false;
        float override = IShoulderSurfing.getInstance().getClientConfig().getCameraConfig().getFovOverride();
        double wanted = override * Mth.lerp(ssrcamerafixes$partialTick, oldFov, fov);
        double got = Minecraft.getInstance().options.fov().get() * Mth.lerp(ssrcamerafixes$ssrPartialTick, oldFov, fov);
        if (Math.abs(got) < 1.0E-4) return wanted;
        return value * wanted / got;
    }

    @Unique
    private static boolean ssrcamerafixes$overrideActive() {
        IShoulderSurfing ssr = IShoulderSurfing.getInstance();
        ICameraConfig config = ssr.getClientConfig().getCameraConfig();
        return ssr.isShoulderSurfing() && config.isFovOverrideEnabled();
    }
}
