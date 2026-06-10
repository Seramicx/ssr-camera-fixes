package com.ssrcamerafixes.fabric1211.handler;

import com.ssrcamerafixes.SsrCameraFixesConfig;
import com.ssrcamerafixes.SsrCameraFixesConfig.IdleBehavior;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.compat.WizardsHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;

public final class SprintRotateHandler {

    public static final SprintRotateHandler INSTANCE = new SprintRotateHandler();

    private static volatile boolean active = false;
    private static volatile float savedYaw = 0F;

    private SprintRotateHandler() {}

    public static boolean isActive() { return active; }

    public static void applyAfterInputTick(Minecraft mc, Input input) {
        active = false;

        LocalPlayer player = mc.player;
        if (player == null) return;
        if (mode() == IdleBehavior.DECOUPLED) return;
        if (mc.options.getCameraType() != CameraType.THIRD_PERSON_BACK) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!player.isSprinting()) return;
        if (player.isUsingItem() || player.isBlocking()) return;
        if (WizardsHelper.isCasting()) return;

        float rawForward = 0F;
        if (mc.options.keyUp.isDown()) rawForward += 1F;
        if (mc.options.keyDown.isDown()) rawForward -= 1F;

        float rawStrafe = 0F;
        if (mc.options.keyLeft.isDown()) rawStrafe += 1F;
        if (mc.options.keyRight.isDown()) rawStrafe -= 1F;

        float rawMagnitude = Mth.sqrt(rawForward * rawForward + rawStrafe * rawStrafe);
        if (rawMagnitude < 0.01F) return;

        float cameraYaw = ShoulderSurfingHelper.getCameraYaw();
        float offsetAngle = -(float) Math.toDegrees(Math.atan2(rawStrafe, rawForward));
        float bodyYaw = Mth.wrapDegrees(cameraYaw + offsetAngle);

        savedYaw = cameraYaw;
        player.setYRot(bodyYaw);

        float modMagnitude = Mth.sqrt(input.forwardImpulse * input.forwardImpulse
                + input.leftImpulse * input.leftImpulse);
        float magnitude = Math.min(rawMagnitude, modMagnitude);
        input.forwardImpulse = magnitude;
        input.leftImpulse = 0F;

        active = true;
    }

    private static IdleBehavior mode() {
        try {
            return SsrCameraFixesConfig.IDLE_BEHAVIOR.get();
        } catch (Throwable t) {
            return IdleBehavior.DECOUPLED;
        }
    }

    public void onClientTickEnd(Minecraft mc) {
        if (!active) return;
        LocalPlayer player = mc.player;
        if (player == null) {
            active = false;
            return;
        }
        player.setYRot(savedYaw);
        player.yRotO = savedYaw;
        player.yHeadRot = player.yBodyRot;
        player.yHeadRotO = player.yBodyRotO;
        active = false;
    }
}
