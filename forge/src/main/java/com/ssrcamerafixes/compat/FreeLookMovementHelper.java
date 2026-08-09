package com.ssrcamerafixes.compat;

import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfingCamera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;

public final class FreeLookMovementHelper {

    private FreeLookMovementHelper() {}

    public static void applyToInput(Input input) {
        Vec2 rotated = cameraRelativeMoveVector(input.leftImpulse, input.forwardImpulse);
        if (rotated == null) {
            return;
        }
        input.leftImpulse = rotated.x;
        input.forwardImpulse = rotated.y;
    }

    private static Vec2 cameraRelativeMoveVector(float leftImpulse, float forwardImpulse) {
        IShoulderSurfing ssr = ShoulderSurfingHelper.instanceOrNull();
        if (ssr == null) {
            return null;
        }
        if (!ssr.isFreeLooking()) {
            return null;
        }
        if (!ssr.isShoulderSurfing() || !ssr.isCameraDecoupled() || ShoulderSurfingHelper.isLookFollowingCrosshairTarget()) {
            return null;
        }
        if (leftImpulse * leftImpulse + forwardImpulse * forwardImpulse <= 0F) {
            return null;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        Entity cameraEntity = mc.getCameraEntity();
        if (player == null || cameraEntity != player) {
            return null;
        }
        if (isForcingVanillaInput(cameraEntity)) {
            return null;
        }

        IShoulderSurfingCamera camera = ssr.getCamera();
        if (camera == null) {
            return null;
        }

        float delta = Mth.degreesDifference(player.getYRot(), camera.getYRot()) * Mth.DEG_TO_RAD;
        float sin = Mth.sin(delta);
        float cos = Mth.cos(delta);
        return new Vec2(leftImpulse * cos - forwardImpulse * sin, forwardImpulse * cos + leftImpulse * sin);
    }

    private static boolean isForcingVanillaInput(Entity cameraEntity) {
        try {
            Class<?> hooks = Class.forName("com.github.exopandora.shouldersurfing.client.EventHooks");
            return (boolean) hooks.getMethod("isForcingVanillaPlayerInput", Entity.class).invoke(null, cameraEntity);
        } catch (Throwable t) {
            return false;
        }
    }
}
