package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.SsrCameraFixesMod;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SsrCameraFixesMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ShoulderCycleHandler {

    public enum Mode { RIGHT, LEFT, OVERHEAD }

    private static volatile boolean isOverhead = false;

    private ShoulderCycleHandler() {}

    public static Mode getMode() {
        if (isOverhead) return Mode.OVERHEAD;
        return ShoulderSurfingHelper.getStoredShoulderX() >= 0.0 ? Mode.RIGHT : Mode.LEFT;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
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
