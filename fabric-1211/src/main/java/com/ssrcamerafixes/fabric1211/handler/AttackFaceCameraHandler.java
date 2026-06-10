package com.ssrcamerafixes.fabric1211.handler;

import com.ssrcamerafixes.compat.BetterCombatHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class AttackFaceCameraHandler {

    public static final AttackFaceCameraHandler INSTANCE = new AttackFaceCameraHandler();

    private static float camYawO = Float.NaN;
    private static boolean hadAttackO = false;

    private AttackFaceCameraHandler() {}

    public void onClientTickStart(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (!shouldSnap(mc, player)) return;
        snapToCamera(player, false);
    }

    public void onClientTickEnd(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (!shouldSnap(mc, player)) {
            hadAttackO = false;
            camYawO = Float.NaN;
            return;
        }
        snapToCamera(player, true);
    }

    private static boolean shouldSnap(Minecraft mc, LocalPlayer player) {
        if (mc.options.getCameraType() != CameraType.THIRD_PERSON_BACK) return false;
        return player.swinging || BetterCombatHelper.isAttackInProgress();
    }

    private static void snapToCamera(LocalPlayer player, boolean isPostTick) {
        float camYaw = ShoulderSurfingHelper.getCameraYaw();
        player.setYRot(camYaw);
        if (isPostTick) {
            float prev = hadAttackO && !Float.isNaN(camYawO) ? camYawO : camYaw;
            player.yBodyRotO = prev;
            player.yHeadRotO = prev;
            player.yBodyRot = camYaw;
            player.yHeadRot = camYaw;
            camYawO = camYaw;
            hadAttackO = true;
        } else {
            player.yBodyRot = camYaw;
            player.yHeadRot = camYaw;
        }
    }
}
