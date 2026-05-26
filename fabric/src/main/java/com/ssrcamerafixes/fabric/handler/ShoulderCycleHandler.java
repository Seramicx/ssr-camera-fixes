package com.ssrcamerafixes.fabric.handler;

import com.github.exopandora.shouldersurfing.client.InputHandler;
import com.mojang.blaze3d.platform.InputConstants;
import com.ssrcamerafixes.Keybinds;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ShoulderCycleHandler {

    public enum Mode { RIGHT, LEFT, OVERHEAD }

    private static volatile boolean isOverhead = false;

    private static boolean prevSharedKeyDown = false;

    private ShoulderCycleHandler() {}

    public static Mode getMode() {
        if (isOverhead) return Mode.OVERHEAD;
        return ShoulderSurfingHelper.getStoredShoulderX() >= 0.0 ? Mode.RIGHT : Mode.LEFT;
    }

    public static void onClientTickEnd(Minecraft mc) {
        if (Keybinds.SHOULDER_CYCLE == null) return;
        if (mc.player == null || mc.screen != null) return;

        boolean conflict = keysConflict();

        if (conflict) {
            drainSsrSwapShoulder();
            if (consumeSharedKeyRisingEdge()) {
                advance();
                showToast(mc);
            }
        } else {
            prevSharedKeyDown = false;
            while (Keybinds.SHOULDER_CYCLE.consumeClick()) {
                advance();
                showToast(mc);
            }
        }
    }

    private static boolean keysConflict() {
        try {
            InputConstants.Key ours = KeyBindingHelper.getBoundKeyOf(Keybinds.SHOULDER_CYCLE);
            InputConstants.Key ssr = KeyBindingHelper.getBoundKeyOf(InputHandler.SWAP_SHOULDER);
            return ours != null && ours.equals(ssr);
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean consumeSharedKeyRisingEdge() {
        boolean down;
        try {
            down = InputHandler.SWAP_SHOULDER.isDown();
        } catch (Throwable t) {
            return false;
        }
        boolean rising = down && !prevSharedKeyDown;
        prevSharedKeyDown = down;
        return rising;
    }

    private static void drainSsrSwapShoulder() {
        try {
            //noinspection StatementWithEmptyBody
            while (InputHandler.SWAP_SHOULDER.consumeClick()) {}
        } catch (Throwable ignored) {}
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
