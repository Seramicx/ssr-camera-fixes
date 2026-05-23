package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.SsrCameraFixesMod;
import com.ssrcamerafixes.compat.BetterCombatHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Two attack-time facing modes: Better Combat path writes yRot/yBodyRot/yHeadRot
// to camYaw each tick so BC's TargetFinder reads the camera direction. On END
// tick the BC path also writes yBodyRotO/yHeadRotO with the previous tick's
// snapped yaw so render interpolation reads prev→current instead of stale.
// Vanilla/Epic Fight swing path force-aligns body+head to yRot (and zeroes *O)
// to defeat the 50° tickHeadTurn clamp when SSR has decoupled yRot from yBodyRot.
@Mod.EventBusSubscriber(modid = SsrCameraFixesMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AttackFaceCameraHandler {

    private static final Minecraft MC = Minecraft.getInstance();

    private static float prevTickCamYaw = Float.NaN;
    private static boolean hadAttackLastTick = false;

    private AttackFaceCameraHandler() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClientTickStart(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        LocalPlayer player = MC.player;
        if (player == null) return;
        if (!shouldSnap(player)) return;
        snapToCamera(player, false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!event.side.isClient()) return;
        LocalPlayer player = MC.player;
        if (player == null || event.player != player) return;
        if (!shouldSnap(player)) {
            hadAttackLastTick = false;
            prevTickCamYaw = Float.NaN;
            return;
        }
        snapToCamera(player, true);
    }

    private static boolean shouldSnap(LocalPlayer player) {
        if (MC.options.getCameraType() == CameraType.FIRST_PERSON) return false;
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
    }
}
