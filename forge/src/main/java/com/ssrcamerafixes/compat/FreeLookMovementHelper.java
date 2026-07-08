package com.ssrcamerafixes.compat;

import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfingCamera;
import com.github.exopandora.shouldersurfing.api.math.Vec2f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;

public final class FreeLookMovementHelper {

    private FreeLookMovementHelper() {}

    public static void applyToInput(Input input) {
        Vec2f rotated = cameraRelativeMoveVector(new Vec2f(input.getMoveVector()));
        if (rotated == null) {
            return;
        }
        input.leftImpulse = rotated.x();
        input.forwardImpulse = rotated.y();
    }

    public static Vec2f cameraRelativeMoveVector(Vec2f moveVector) {
        IShoulderSurfing ssr;
        try {
            ssr = IShoulderSurfing.getInstance();
        } catch (Throwable t) {
            return null;
        }
        if (!ssr.isFreeLooking()) {
            return null;
        }
        if (!ssr.isShoulderSurfing() || !ssr.isCameraDecoupled() || ssr.isLookFollowingCrosshairTarget()) {
            return null;
        }
        if (moveVector.lengthSquared() <= 0F) {
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

        float bodyYaw = player.getYRot();
        return moveVector.rotateDegrees(Mth.degreesDifference(bodyYaw, camera.getYRot()));
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
