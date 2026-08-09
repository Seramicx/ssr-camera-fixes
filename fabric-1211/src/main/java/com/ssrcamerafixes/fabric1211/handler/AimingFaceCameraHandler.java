package com.ssrcamerafixes.fabric1211.handler;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.compat.WizardsHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

public final class AimingFaceCameraHandler {

    private AimingFaceCameraHandler() {}

    public static void onClientTickStart(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!WizardsHelper.isCasting() && !WizardsHelper.isMeleeSkillActive()) return;
        if (isControllingMobMount(player)) return;

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
        if (!player.isBlocking() && !WizardsHelper.isCastingLive() && !WizardsHelper.isMeleeSkillActive()) return;
        if (isControllingMobMount(player)) return;

        float camYaw = ShoulderSurfingHelper.getCameraYaw();
        player.setYRot(camYaw);
        player.yBodyRot = camYaw;
        player.yHeadRot = camYaw;
    }

    // Spell Engine releases the cast from LocalPlayer.tick() TAIL and the server aims it from the player's synced
    // rotation, which mounted is Better Mount Steering's offset yaw. The vanilla move packet leaves during that same
    // tick, so aim at the crosshair at START (before the move packet) rather than END, or the release reads the old
    // yaw. BMS re-takes the body yaw afterwards, but the cast direction is already captured
    public static void onMountedTickStart(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!isControllingMobMount(player)) return;
        if (!WizardsHelper.isCastingLive()) return;

        ShoulderSurfingHelper.lookAtCrosshairTarget();

        ClientPacketListener conn = mc.getConnection();
        if (conn != null) {
            conn.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.onGround()));
        }
    }

    public static void faceCrosshairAndSync(Minecraft mc, LocalPlayer player) {
        ShoulderSurfingHelper.lookAtCrosshairTarget();

        ClientPacketListener conn = mc.getConnection();
        if (conn != null) {
            conn.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.onGround()));
        }
    }

    private static boolean isControllingMobMount(LocalPlayer player) {
        Entity v = player.getVehicle();
        return v instanceof Mob mob && mob.getControllingPassenger() == player;
    }
}
