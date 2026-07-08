package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.EpicFightHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class WallClimbBodyLockHandler {

    public static final WallClimbBodyLockHandler INSTANCE = new WallClimbBodyLockHandler();

    private static Float lockedBodyYaw = null;

    private WallClimbBodyLockHandler() {}

    // LOWEST so body-yaw lock applies after movement input handlers
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerTickEnd(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!event.side.isClient()) return;
        LocalPlayer player = Minecraft.getInstance().player;
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
