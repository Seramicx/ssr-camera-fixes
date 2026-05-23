package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.SsrCameraFixesMod;
import com.ssrcamerafixes.compat.EpicFightHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Pins yBodyRot/yHeadRot during WoM spider-techniques wall-climb so the
// player model stays facing the wall regardless of mouse rotation.
@Mod.EventBusSubscriber(modid = SsrCameraFixesMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class WallClimbBodyLockHandler {

    private static final Minecraft MC = Minecraft.getInstance();

    private static Float lockedBodyYaw = null;

    private WallClimbBodyLockHandler() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTickEnd(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!event.side.isClient()) return;
        LocalPlayer player = MC.player;
        if (player == null || event.player != player) return;

        if (EpicFightHelper.isWallClimbing(player)) {
            if (lockedBodyYaw == null) {
                lockedBodyYaw = player.yBodyRot;
            }
            float lock = lockedBodyYaw;
            player.yBodyRot = lock;
            player.yBodyRotO = lock;
            player.yHeadRot = lock;
            player.yHeadRotO = lock;
        } else if (lockedBodyYaw != null) {
            lockedBodyYaw = null;
        }
    }
}
