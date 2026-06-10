package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.SsrCameraFixesMod;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public final class ShoulderCycleHandler {

    public static final ShoulderCycleHandler INSTANCE = new ShoulderCycleHandler();

    public enum Mode { RIGHT, LEFT, OVERHEAD }

    private static volatile boolean isOverhead = false;

    private ShoulderCycleHandler() {}

    public static Mode getMode() {
        if (isOverhead) return Mode.OVERHEAD;
        return ShoulderSurfingHelper.getStoredShoulderX() >= 0.0 ? Mode.RIGHT : Mode.LEFT;
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        if (SsrCameraFixesMod.SHOULDER_CYCLE == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        while (SsrCameraFixesMod.SHOULDER_CYCLE.consumeClick()) {
            advance();
            showToast(mc);
        }
    }

    private static void advance() {
        switch (getMode()) {
            case RIGHT -> ShoulderSurfingHelper.swapShoulder();
            case LEFT -> isOverhead = true;
            case OVERHEAD -> {
                isOverhead = false;
                ShoulderSurfingHelper.swapShoulder();
            }
        }
    }

    private static void showToast(Minecraft mc) {
        if (mc.player == null) return;
        String label = switch (getMode()) {
            case LEFT -> "left";
            case OVERHEAD -> "overhead";
            default -> "right";
        };
        mc.player.displayClientMessage(
                Component.literal("Shoulder: ")
                        .append(Component.literal(label).withStyle(ChatFormatting.AQUA)),
                true);
    }
}
