package com.ssrcamerafixes.fabric1211.handler;

import com.ssrcamerafixes.compat.BetterCombatHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class AttackFaceCameraHandler {

    private static float prevTickCamYaw = Float.NaN;
    private static boolean hadAttackLastTick = false;

    private AttackFaceCameraHandler() {}

    public static void onClientTickStart(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (!shouldSnap(mc, player)) return;
        snapToCamera(player, false);
    }

    public static void onClientTickEnd(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (!shouldSnap(mc, player)) {
            hadAttackLastTick = false;
            prevTickCamYaw = Float.NaN;
            return;
        }
        snapToCamera(player, true);
    }

    private static boolean shouldSnap(Minecraft mc, LocalPlayer player) {
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return false;
        return player.swinging || BetterCombatHelper.isAttackInProgress();
    }

    private static void snapToCamera(LocalPlayer player, boolean isPostTick) {
        if (BetterCombatHelper.isAttackInProgress()) {
            float camYaw = ShoulderSurfingHelper.getCameraYaw();
            player.setYRot(camYaw);
            if (isPostTick) {
                float prev = hadAttackLastTick && !Float.isNaN(prevTickCamYaw) ? prevTickCamYaw : camYaw;
                player.yBodyRotO = prev;
                player.yHeadRotO = prev;
                player.yBodyRot = camYaw;
                player.yHeadRot = camYaw;
                prevTickCamYaw = camYaw;
                hadAttackLastTick = true;
            } else {
                player.yBodyRot = camYaw;
                player.yHeadRot = camYaw;
            }
            return;
        }
        float yRot = player.getYRot();
        player.yBodyRot = yRot;
        player.yBodyRotO = yRot;
        player.yHeadRot = yRot;
        player.yHeadRotO = yRot;
        if (isPostTick) {
            hadAttackLastTick = false;
            prevTickCamYaw = Float.NaN;
        }
    }
}
