package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// WoM aims the swim impulse by body yaw, wrong under a decoupled camera; rotate that impulse into the camera frame.
public final class WomAquaCameraFrameHandler {

    public static final WomAquaCameraFrameHandler INSTANCE = new WomAquaCameraFrameHandler();

    private boolean active;
    private Vec3 savedDelta = Vec3.ZERO;
    private float savedCamYaw;
    private float savedViewYaw;

    private WomAquaCameraFrameHandler() {}

    // HIGHEST: snapshot the delta before WoM's input listener adds its impulse this tick.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onInputPre(MovementInputUpdateEvent event) {
        active = false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!ShoulderSurfingHelper.isCameraDecoupled()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;
        if (!EpicFightHelper.isWomMoverSwimming(player)) return;

        savedDelta = player.getDeltaMovement();
        savedCamYaw = ShoulderSurfingHelper.getCameraYaw();
        savedViewYaw = player.getViewYRot(1.0F);
        active = true;
    }

    // LOWEST: WoM has added its impulse now; rotate it into the camera frame.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onInputPost(MovementInputUpdateEvent event) {
        if (!active) return;
        active = false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        Vec3 dm = player.getDeltaMovement();
        Vec3 carry = savedDelta.scale(0.95);
        Vec3 impulse = dm.subtract(carry);
        if (impulse.x * impulse.x + impulse.z * impulse.z < 1.0E-8) return;

        double delta = Math.toRadians(Mth.wrapDegrees(savedCamYaw - savedViewYaw));
        double cos = Math.cos(delta);
        double sin = Math.sin(delta);
        double nx = impulse.x * cos - impulse.z * sin;
        double nz = impulse.x * sin + impulse.z * cos;
        player.setDeltaMovement(carry.x + nx, dm.y, carry.z + nz);
    }
}
