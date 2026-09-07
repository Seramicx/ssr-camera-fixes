package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.HeldCrossbowAim;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// Adaptive + isAiming makes SSR set isCameraDecoupled=false and copy camera → player on ClientTick START.
// Freezing body rot at END locked facing (look-free but walk locked). Instead: save before SSR, then on
// LOWEST START (after SSR) undo the camera copy and force isCameraDecoupled=true so the rest of the tick
// — movement remap included — runs decoupled. Adaptive crosshair stays on via isAiming.
public final class ChargedCrossbowDecoupleHandler {

    public static final ChargedCrossbowDecoupleHandler INSTANCE = new ChargedCrossbowDecoupleHandler();

    private float savedYRot;
    private float savedXRot;
    private float savedYBodyRot;
    private float savedYHeadRot;
    private boolean saved;

    private ChargedCrossbowDecoupleHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientTickStartBeforeSsr(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
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

    // SSR Forge ClientEventHandler.tick also runs on START (default priority). Run after it.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onClientTickStartAfterSsr(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START || !saved) return;
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
