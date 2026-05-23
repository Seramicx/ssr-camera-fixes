package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.SsrCameraFixesMod;
import com.ssrcamerafixes.compat.BetterCombatHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = SsrCameraFixesMod.MODID, value = Dist.CLIENT)
public final class AttackFaceCameraHandler {

    private static final Minecraft MC = Minecraft.getInstance();

    private static float prevTickCamYaw = Float.NaN;
    private static boolean hadAttackLastTick = false;

    private AttackFaceCameraHandler() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClientTickStart(ClientTickEvent.Pre event) {
        LocalPlayer player = MC.player;
        if (player == null) return;
        if (!shouldSnap(player)) return;
        snapToCamera(player, false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) return;
        if (player != MC.player) return;
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
        if (isPostTick) {
            hadAttackLastTick = false;
            prevTickCamYaw = Float.NaN;
        }
    }
}
