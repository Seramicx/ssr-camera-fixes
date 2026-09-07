package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.HeldCrossbowAim;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

// Adaptive + isAiming makes SSR set isCameraDecoupled=false and copy camera → player each tick.
// Freezing body rot locked facing. Undo the camera copy after SSR and force isCameraDecoupled=true
// so movement remap runs decoupled while adaptive crosshair stays on via isAiming.
public final class ChargedCrossbowDecoupleHandler {

    public static final ChargedCrossbowDecoupleHandler INSTANCE = new ChargedCrossbowDecoupleHandler();

    private float savedYRot;
    private float savedXRot;
    private float savedYBodyRot;
    private float savedYHeadRot;
    private boolean saved;

    private ChargedCrossbowDecoupleHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientTickPreBeforeSsr(ClientTickEvent.Pre event) {
        saved = false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!HeldCrossbowAim.isChargedPassiveHold(player)) return;
        savedYRot = player.getYRot();
        savedXRot = player.getXRot();
        savedYBodyRot = player.yBodyRot;
        savedYHeadRot = player.yHeadRot;
        saved = true;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onClientTickPreAfterSsr(ClientTickEvent.Pre event) {
        if (!saved) return;
        saved = false;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!HeldCrossbowAim.isChargedPassiveHold(player)) return;

        player.setYRot(savedYRot);
        player.setXRot(savedXRot);
        player.yBodyRot = savedYBodyRot;
        player.yBodyRotO = savedYBodyRot;
        player.yHeadRot = savedYHeadRot;
        player.yHeadRotO = savedYHeadRot;

        ShoulderSurfingHelper.forceCameraDecoupled(true);
        ShoulderSurfingHelper.clearLookFollowingCrosshairTarget();
    }
}
