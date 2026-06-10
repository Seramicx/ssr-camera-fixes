package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.SsrCameraFixesConfig;
import com.ssrcamerafixes.SsrCameraFixesConfig.IdleBehavior;
import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.compat.TaczHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class SprintRotateHandler {

    public static final SprintRotateHandler INSTANCE = new SprintRotateHandler();

    private static boolean active = false;
    private static float savedYaw = 0F;

    private SprintRotateHandler() {}

    public static boolean isActive() { return active; }

    // LOWEST so sprint-back rotation override wins over vanilla input processing
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMovementInput(MovementInputUpdateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        active = false;

        if (mode() == IdleBehavior.DECOUPLED) return;
        if (EpicFightHelper.isLockOnTargeting()) return;
        if (mc.options.getCameraType() != CameraType.THIRD_PERSON_BACK) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!player.isSprinting()) return;

        if (EpicFightHelper.isAiming(player) || player.isUsingItem() || player.isBlocking()) return;
        if (TaczHelper.isAimingOrFiring()) return;

        if (EpicFightHelper.animationOwnsLivingMotion(player)) return;

        Input input = event.getInput();

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

    // LOWEST so sprint-back rotation override wins over vanilla input processing
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!event.side.isClient()) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || event.player != player) return;

        if (active) {
            player.setYRot(savedYaw);
            player.yRotO = savedYaw;
            player.yHeadRot = player.yBodyRot;
            player.yHeadRotO = player.yBodyRotO;
            active = false;
        }
    }
}
