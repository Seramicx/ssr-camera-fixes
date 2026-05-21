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

/**
 * Lock {@code yBodyRot}/{@code yHeadRot} while in a WOM spider-techniques
 * wall-climb animation so the player model stays facing the wall regardless
 * of mouse rotation.
 *
 * <p><b>What this fixes:</b> the spider techniques skill reads
 * {@code player.f_20885_} ({@code yBodyRot}) for two things — the
 * wall-detection ray and the climb's {@code deltaMovement} direction. With
 * no fix, mouse rotation (vanilla / SSR-coupled) updates {@code yRot},
 * vanilla {@code tickHeadTurn} lerps {@code yBodyRot} toward it, the ray
 * and movement vector both rotate off the wall, and the player twists
 * away from the wall and can clip into it.
 *
 * <p>Approach: capture {@code yBodyRot} on entry into wall-climb; force
 * {@code yBodyRot}/{@code yBodyRotO}/{@code yHeadRot}/{@code yHeadRotO} to
 * the captured value at {@code PlayerTickEvent.END} (LOWEST). That fires
 * after vanilla {@code tickHeadTurn} inside {@code LivingEntity.tick}, so
 * our writes override the per-tick lerp. {@code player.yRot} is left
 * untouched — the camera (and {@code yRot} in coupled/vanilla mode) can
 * rotate freely; only the body model is pinned.
 *
 * <p>Detection: {@link EpicFightHelper#isWallClimbing(LocalPlayer)} — see
 * its javadoc for the four-condition gate.
 *
 * <p>SSR decoupled mode: this is a no-op in practice. Mouse there only
 * rotates the SSR camera; {@code player.yRot} stays stable; vanilla
 * {@code tickHeadTurn} has nothing to lerp toward; {@code yBodyRot} would
 * already stay still. The lock writes the same value it would have been
 * anyway.
 */
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
