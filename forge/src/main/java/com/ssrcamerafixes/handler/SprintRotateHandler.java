package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.SsrCameraFixesMod;
import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Sprint-rotate in third-person on foot: rewrite player.yRot to the body
// direction during the tick, restore camera yaw on PlayerTickEvent.END.
@Mod.EventBusSubscriber(modid = SsrCameraFixesMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class SprintRotateHandler {

    private static final Minecraft MC = Minecraft.getInstance();

    private static boolean active = false;
    private static float savedYaw = 0F;

    private SprintRotateHandler() {}

    public static boolean isActive() { return active; }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onMovementInput(MovementInputUpdateEvent event) {
        LocalPlayer player = MC.player;
        if (player == null) return;

        active = false;

        if (EpicFightHelper.isLockOnTargeting()) return;
        if (MC.options.getCameraType() != CameraType.THIRD_PERSON_BACK) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!player.isSprinting()) return;

        if (EpicFightHelper.isAiming(player) || player.isUsingItem() || player.isBlocking()) return;

        if (EpicFightHelper.animationOwnsLivingMotion(player)) return;

        Input input = event.getInput();

        float rawForward = 0F;
        if (MC.options.keyUp.isDown()) rawForward += 1F;
        if (MC.options.keyDown.isDown()) rawForward -= 1F;

        float rawStrafe = 0F;
        if (MC.options.keyLeft.isDown()) rawStrafe += 1F;
        if (MC.options.keyRight.isDown()) rawStrafe -= 1F;

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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!event.side.isClient()) return;

        LocalPlayer player = MC.player;
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
