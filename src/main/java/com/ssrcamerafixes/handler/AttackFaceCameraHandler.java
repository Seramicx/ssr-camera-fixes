package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.SsrCameraFixesMod;
import com.ssrcamerafixes.compat.BetterCombatHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Two attack-time facing modes, picked by which mod is driving the swing.
 *
 * <p><b>Better Combat path</b> — write {@code yRot}, {@code yBodyRot},
 * {@code yHeadRot} all to {@code cameraYaw} every tick. {@code *O} fields
 * are left to vanilla's tick-start capture so render interpolation reads
 * (prev-tick value → this-tick value) and the body/head visibly track the
 * camera smoothly between ticks. BC's {@code TargetFinder} reads
 * {@code player.getYaw()} inside {@code performAttack} so the hitbox aims
 * where the camera points. Writing all three (not just {@code yRot}) is
 * required because vanilla never updates {@code yHeadRot} per-tick
 * client-side for the LocalPlayer — {@code Player.serverAiStep} only fires
 * when {@code isEffectiveAi() == !level().isClientSide}.
 *
 * <p><b>Epic Fight / vanilla swing path</b> — aggressive body snap. When
 * {@code player.swinging} is true and BC is not in progress, force
 * {@code yBodyRot = yHeadRot = yRot} and zero the {@code O} fields. Needed
 * because vanilla {@code tickHeadTurn}'s 50° {@code |yRot - yBodyRot|}
 * clamp would otherwise leave the body stuck partway when SSR has decoupled
 * {@code yRot} far from {@code yBodyRot} at swing start. SSR's own
 * {@code lookAtCrosshairTargetInternal} drives {@code yRot} during these
 * swings.
 *
 * <p>Two firing points, both LOWEST priority so we run after every other
 * mod's rotation writes within the same event:
 *
 * <ol>
 *   <li><b>{@code ClientTickEvent.START}</b> — runs before
 *       {@code Minecraft.tick()} body. For BC, this is the write BC's
 *       {@code pre_Tick} HEAD inject reads at {@code performAttack}.</li>
 *   <li><b>{@code PlayerTickEvent.END}</b> — runs after {@code aiStep}.
 *       For BC, re-applies the snap after {@code tickHeadTurn} and any
 *       other writers within the tick. For vanilla/EF swings, re-applies
 *       the body/head snap after {@code tickHeadTurn}.</li>
 * </ol>
 *
 * <p>Active condition: not 1st person AND ({@code player.swinging} OR
 * {@link BetterCombatHelper#isAttackInProgress()}). 1st person is excluded
 * so the BLO + lockon-movement-fix 1st-person handler stays in charge there.
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
        // Better Combat path: snap yRot, yBodyRot, yHeadRot all to camera
        // every tick — same pattern as AimingFaceCameraHandler. Required
        // because vanilla never updates yHeadRot per-tick on the client for
        // the LocalPlayer (Player.serverAiStep — which does yHeadRot = yRot
        // — only fires when isEffectiveAi(), i.e. server-side). Without
        // writing yHeadRot ourselves, the head would freeze at its last
        // value while yBodyRot lerps, producing visible head-vs-body twist
        // during the swing. The *O fields are left for vanilla to capture
        // at tick start so render interpolation stays smooth (camera moves
        // → yRotO bumped by mouse turn() → prev-tick body/head captured →
        // this-tick body/head = camYaw → renderer lerps between them).
        // BC's TargetFinder reads player.getYaw() inside performAttack so
        // the hitbox aims where the camera points.
        if (BetterCombatHelper.isAttackInProgress()) {
            float camYaw = ShoulderSurfingHelper.getCameraYaw();
            player.setYRot(camYaw);
            player.yBodyRot = camYaw;
            player.yHeadRot = camYaw;
            return;
        }
        // Vanilla swing / Epic Fight path: aggressive body snap. EpicFight's
        // own systems depend on this to keep the body aligned with yRot
        // throughout its attacks, and vanilla's 50° tickHeadTurn clamp
        // would otherwise leave the body "stuck partway" when SSR has
        // decoupled yRot far from yBodyRot at swing start.
        float yRot = player.getYRot();
        player.yBodyRot = yRot;
        player.yBodyRotO = yRot;
        player.yHeadRot = yRot;
        player.yHeadRotO = yRot;
    }
}
