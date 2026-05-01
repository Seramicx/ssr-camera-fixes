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

/**
 * RIGHT → LEFT → OVERHEAD shoulder cycle on the {@code SHOULDER_CYCLE} keybind.
 *
 * <p>SSR's preset cycling is per-axis (X presets, Y presets, Z presets cycled
 * independently), which can't represent a coupled "X=0 + high Y" overhead
 * preset. This cycle layers one on top of SSR by:
 * <ul>
 *   <li>RIGHT → LEFT: calls SSR's {@code swapShoulder()} to flip its X.
 *   <li>LEFT → OVERHEAD: leaves SSR alone (X stays negative); the
 *       {@link com.ssrcamerafixes.compat.SsrCameraFixesPlugin OverheadOffsetCallback}
 *       overrides the offset to {@code (0, overheadY, original.z)}.
 *   <li>OVERHEAD → RIGHT: calls {@code swapShoulder()} again to flip X back to positive.
 * </ul>
 *
 * <p>Public {@link #getMode()} is read by the SSR plugin's offset callback.
 */
@Mod.EventBusSubscriber(modid = SsrCameraFixesMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ShoulderCycleHandler {

    public enum Mode { RIGHT, LEFT, OVERHEAD }

    private static volatile Mode current = Mode.RIGHT;

    private ShoulderCycleHandler() {}

    public static Mode getMode() { return current; }

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
        switch (current) {
            case RIGHT -> {
                ShoulderSurfingHelper.swapShoulder();
                current = Mode.LEFT;
            }
            case LEFT -> current = Mode.OVERHEAD;
            case OVERHEAD -> {
                ShoulderSurfingHelper.swapShoulder();
                current = Mode.RIGHT;
            }
        }
    }

    private static void showToast(Minecraft mc) {
        if (mc.player == null) return;
        String label = switch (current) {
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
