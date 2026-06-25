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

    private static volatile boolean isOverhead = false;

    private boolean sharedKeyDownO = false;

    private ShoulderCycleHandler() {}

    public static Mode getMode() {
        if (isOverhead) return Mode.OVERHEAD;
        return ShoulderSurfingHelper.getStoredShoulderX() >= 0.0 ? Mode.RIGHT : Mode.LEFT;
    }

    // HIGHEST so the shared-key case resolves before ShoulderSurfingImpl.tick consumes SWAP_SHOULDER on START
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        if (SsrCameraFixesMod.SHOULDER_CYCLE == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;

        if (sharesKeyWithSsrSwap()) {
            // SSR's bind wins KeyMapping.MAP since it registers before our RegisterKeyMappingsEvent,
            // so its clicks must be drained here and the cycle advanced off the shared key's rising edge
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
