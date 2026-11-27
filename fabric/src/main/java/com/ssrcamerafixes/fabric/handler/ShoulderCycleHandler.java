package com.ssrcamerafixes.fabric.handler;

import com.github.exopandora.shouldersurfing.client.InputHandler;
import com.mojang.blaze3d.platform.InputConstants;
import com.ssrcamerafixes.Keybinds;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.mixin.AccessorKeyMapping;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ShoulderCycleHandler {

    public static final ShoulderCycleHandler INSTANCE = new ShoulderCycleHandler();

    public enum Mode { RIGHT, LEFT, OVERHEAD }

    // SSR's default offset_x is -0.75, camera over the right shoulder; positive X is the other shoulder
    private static volatile Mode mode = Mode.RIGHT;
    private static volatile boolean syncedFromConfig;

    private static boolean sharedKeyDownO = false;

    private ShoulderCycleHandler() {}

    public static Mode getMode() {
        return mode;
    }

    public static double applySide(double x) {
        return mode == Mode.LEFT ? Math.abs(x) : -Math.abs(x);
    }

    // Called from InputHandler.tick HEAD so SSR never sees the shared click
    public static void drainSsrSwapIfConflicting() {
        if (!keysConflict()) return;
        try {
            //noinspection StatementWithEmptyBody
            while (InputHandler.SWAP_SHOULDER.consumeClick()) {}
        } catch (Throwable ignored) {}
    }

    public void onClientTickStart(Minecraft mc) {
        if (Keybinds.SHOULDER_CYCLE == null) return;
        if (mc.player == null || mc.screen != null) return;

        syncFromConfigIfNeeded();

        if (keysConflict()) {
            //noinspection StatementWithEmptyBody
            while (Keybinds.SHOULDER_CYCLE.consumeClick()) {}
            drainSsrSwapIfConflicting();
            if (consumeSharedKeyRisingEdge()) {
                advance();
                showToast(mc);
            }
        } else {
            sharedKeyDownO = false;
            while (Keybinds.SHOULDER_CYCLE.consumeClick()) {
                advance();
                showToast(mc);
            }
        }
    }

    private static void syncFromConfigIfNeeded() {
        if (syncedFromConfig) return;
        syncedFromConfig = true;
        double x = ShoulderSurfingHelper.getStoredShoulderX();
        if (mode != Mode.OVERHEAD) {
            mode = x >= 1.0E-4 ? Mode.LEFT : Mode.RIGHT;
        }
    }

    private static boolean keysConflict() {
        if (Keybinds.SHOULDER_CYCLE == null) return false;
        try {
            InputConstants.Key ours = ((AccessorKeyMapping) (Object) Keybinds.SHOULDER_CYCLE).ssrcamerafixes$getKey();
            InputConstants.Key ssr = ((AccessorKeyMapping) (Object) InputHandler.SWAP_SHOULDER).ssrcamerafixes$getKey();
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
        boolean rising = down && !sharedKeyDownO;
        sharedKeyDownO = down;
        return rising;
    }

    private static void advance() {
        mode = switch (mode) {
            case RIGHT -> Mode.LEFT;
            case LEFT -> Mode.OVERHEAD;
            case OVERHEAD -> Mode.RIGHT;
        };
    }

    private static void showToast(Minecraft mc) {
        if (mc.player == null) return;
        String label = switch (mode) {
            case LEFT -> "left";
            case OVERHEAD -> "overhead";
            case RIGHT -> "right";
        };
        mc.player.displayClientMessage(
                Component.literal("Shoulder: ")
                        .append(Component.literal(label).withStyle(ChatFormatting.AQUA)),
                true);
    }
}
