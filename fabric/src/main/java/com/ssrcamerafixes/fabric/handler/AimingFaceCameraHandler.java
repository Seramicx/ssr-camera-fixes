package com.ssrcamerafixes.fabric.handler;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.fabric.compat.WizardsHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;

public final class AimingFaceCameraHandler {

    private AimingFaceCameraHandler() {}

    public static void onClientTickStart(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!WizardsHelper.isCasting()) return;

        ShoulderSurfingHelper.lookAtCrosshairTarget();

        ClientPacketListener conn = mc.getConnection();
        if (conn != null) {
            conn.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.onGround()));
        }
    }

    public static void applyAfterInputTick(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!player.isBlocking() && !WizardsHelper.isCasting()) return;

        float camYaw = ShoulderSurfingHelper.getCameraYaw();
        player.setYRot(camYaw);
        player.yBodyRot = camYaw;
        player.yHeadRot = camYaw;
    }
}
