package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class PhantomAscentFaceCameraHandler {

    public static final PhantomAscentFaceCameraHandler INSTANCE = new PhantomAscentFaceCameraHandler();

    private boolean jumpDownO = false;

    private PhantomAscentFaceCameraHandler() {}

    // HIGHEST so the snap lands before Epic Fight's input tick reads getViewYRot for the launch direction
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onMovementInput(MovementInputUpdateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            jumpDownO = false;
            return;
        }

        boolean jumpDown = mc.options.keyJump.isDown();
        boolean rising = jumpDown && !jumpDownO;
        jumpDownO = jumpDown;

        if (!rising) return;
        if (!ShoulderSurfingHelper.isCameraDecoupled()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;
        if (!EpicFightHelper.isAirborneSkillContext(player)) return;

        float rawForward = 0F;
        if (mc.options.keyUp.isDown())   rawForward += 1.0F;
        if (mc.options.keyDown.isDown()) rawForward -= 1.0F;

        // EF 1.19.2 has no backward roll and still applies its own strafe rotation, so flip the body 180 only for
        // backward input and let EF handle the diagonal; adding strafe here too would double it onto left/right.
        float offset = rawForward < 0F ? 180F : 0F;

        float camYaw = Mth.wrapDegrees(ShoulderSurfingHelper.getCameraYaw() + offset);
        float camXRot = ShoulderSurfingHelper.getCameraXRot();
        player.setYRot(camYaw);
        player.yRotO = camYaw;
        player.setXRot(camXRot);
        player.xRotO = camXRot;
    }
}
