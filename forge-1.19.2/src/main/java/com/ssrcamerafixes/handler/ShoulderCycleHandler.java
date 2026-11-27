package com.ssrcamerafixes.handler;

import com.github.exopandora.shouldersurfing.client.InputHandler;
import com.mojang.blaze3d.platform.InputConstants;
import com.ssrcamerafixes.SsrCameraFixesMod;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.mixin.AccessorKeyMapping;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ShoulderCycleHandler {

    public static final ShoulderCycleHandler INSTANCE = new ShoulderCycleHandler();

    public enum Mode { RIGHT, LEFT, OVERHEAD }

    // SSR's default offset_x is -0.75, camera over the right shoulder; positive X is the other shoulder
    private static volatile Mode mode = Mode.RIGHT;
    private static volatile boolean syncedFromConfig;

    private boolean sharedKeyDownO = false;

    private ShoulderCycleHandler() {}

    public static Mode getMode() {
        return mode;
    }

    public static double applySide(double x) {
        return mode == Mode.LEFT ? Math.abs(x) : -Math.abs(x);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (SsrCameraFixesMod.SHOULDER_CYCLE == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        syncFromConfigIfNeeded();

        if (sharesKeyWithSsrSwap()) {
            boolean down = InputHandler.SWAP_SHOULDER.isDown();
            boolean rising = down && !sharedKeyDownO;
            sharedKeyDownO = down;
            //noinspection StatementWithEmptyBody
            while (InputHandler.SWAP_SHOULDER.consumeClick()) {}
            if (rising) {
                advance();
                showToast(mc);
            }
        } else {
            sharedKeyDownO = false;
            while (SsrCameraFixesMod.SHOULDER_CYCLE.consumeClick()) {
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

    private static boolean sharesKeyWithSsrSwap() {
        KeyMapping cycle = SsrCameraFixesMod.SHOULDER_CYCLE;
        if (cycle == null) return false;
        try {
            InputConstants.Key ours = ((AccessorKeyMapping) (Object) cycle).ssrcamerafixes$getKey();
            InputConstants.Key ssr = ((AccessorKeyMapping) (Object) InputHandler.SWAP_SHOULDER).ssrcamerafixes$getKey();
            return ours.equals(ssr);
        } catch (Throwable t) {
            return false;
        }
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
