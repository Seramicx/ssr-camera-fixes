package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.SsrCameraFixesMod;
import com.ssrcamerafixes.compat.BetterCombatHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Match the player body and head to {@code player.yRot} during a swing, so
 * the body model fully follows the aim direction (not pinned by vanilla
 * {@code LivingEntity.tickHeadTurn}'s 50° {@code |yRot - yBodyRot|} clamp).
 *
 * <p>We do NOT touch {@code yRot} itself — that's owned by SSR's
 * {@code lookAtCrosshairTargetInternal} when shoulder-surfing
 * (sets it to {@code lookAt(crosshairHitPoint)}, which is the
 * parallax-corrected direction from the player to the visual crosshair so
 * BC's hitbox lands on target) or by vanilla otherwise. Overriding
 * {@code yRot} ourselves was producing the diagonal-attack symptom.
 *
 * <p>Two firing points, both LOWEST priority so we run after every other
 * mod's rotation writes within the same event:
 *
 * <ol>
 *   <li><b>{@code ClientTickEvent.START}</b> — runs before
 *       {@code Minecraft.tick()} body. Ensures body/head match
 *       {@code yRot} before BC's {@code pre_Tick} HEAD inject runs
 *       {@code performAttack}.</li>
 *   <li><b>{@code PlayerTickEvent.END}</b> — runs after
 *       {@code aiStep}, re-applying the match after vanilla
 *       {@code tickHeadTurn} has lerped {@code yBodyRot} away from
 *       {@code yRot}.</li>
 * </ol>
 *
 * <p>Active condition: not 1st person AND ({@code player.swinging} OR
 * {@link BetterCombatHelper#isAttackInProgress()}). 1st person is excluded
 * so the BLO + lockon-movement-fix 1st-person handler stays in charge there.
 *
 * <p>{@code player.swinging} covers vanilla swings.
 * {@link BetterCombatHelper#isAttackInProgress()} covers BC's full
 * upswing+downswing using BC's own
 * {@code MinecraftClient_BetterCombat.isWeaponSwingInProgress()} — accurate
 * for any weapon, vanilla or modded, no hardcoded timing.
 *
 * <p>For BC × SSR specifically: SSR's {@code lookAtCrosshair} only fires
 * while it considers the player "attacking" (default: {@code keyAttack.isDown()}).
 * To keep it firing for the entire BC swing (so {@code yRot} continuously
 * tracks the crosshair-target), {@code SsrCameraFixesPlugin} registers an
 * {@code IPlayerStateCallback} that returns {@code TRUE} for {@code isAttacking}
 * while {@code BetterCombatHelper.isAttackInProgress()} is true.
 */
@Mod.EventBusSubscriber(modid = SsrCameraFixesMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AttackFaceCameraHandler {

    private static final Minecraft MC = Minecraft.getInstance();

    private AttackFaceCameraHandler() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClientTickStart(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        LocalPlayer player = MC.player;
        if (player == null) return;
        if (!shouldSnap(player)) return;
        snapToCamera(player);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!event.side.isClient()) return;
        LocalPlayer player = MC.player;
        if (player == null || event.player != player) return;
        if (!shouldSnap(player)) return;
        snapToCamera(player);
    }

    private static boolean shouldSnap(LocalPlayer player) {
        if (MC.options.getCameraType() == CameraType.FIRST_PERSON) return false;
        return player.swinging || BetterCombatHelper.isAttackInProgress();
    }

    private static void snapToCamera(LocalPlayer player) {
        float yRot = player.getYRot();
        player.yBodyRot = yRot;
        player.yBodyRotO = yRot;
        player.yHeadRot = yRot;
        player.yHeadRotO = yRot;
    }
}
