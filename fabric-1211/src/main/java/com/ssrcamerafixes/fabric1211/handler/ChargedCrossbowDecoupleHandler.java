package com.ssrcamerafixes.fabric1211.handler;

import com.ssrcamerafixes.compat.HeldCrossbowAim;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

// Adaptive + isAiming makes SSR set isCameraDecoupled=false and copy camera → player each tick.
// Driven from MixinSsrCrossbowTickBracket, which straddles SSR's own tick.
public final class ChargedCrossbowDecoupleHandler {

    public static final ChargedCrossbowDecoupleHandler INSTANCE = new ChargedCrossbowDecoupleHandler();

    private float savedYRot;
    private float savedXRot;
    private float savedYBodyRot;
    private float savedYHeadRot;
    private boolean saved;

    private ChargedCrossbowDecoupleHandler() {}

    public void onClientTickStart(Minecraft client) {
        saved = false;
        LocalPlayer player = client.player;
        if (player == null) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!HeldCrossbowAim.isChargedPassiveHold(player)) return;
        savedYRot = player.getYRot();
        savedXRot = player.getXRot();
        savedYBodyRot = player.yBodyRot;
        savedYHeadRot = player.yHeadRot;
        saved = true;
    }

    public void onClientTickAfterSsr(Minecraft client) {
        if (!saved) return;
        saved = false;
        LocalPlayer player = client.player;
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
