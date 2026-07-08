package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.BetterCombatHelper;
import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public final class AttackFaceCameraHandler {

    public static final AttackFaceCameraHandler INSTANCE = new AttackFaceCameraHandler();

    private static float camYawO = Float.NaN;
    private static boolean hadAttackO = false;

    private AttackFaceCameraHandler() {}

    // LOWEST so attack-direction override applies after vanilla input
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onClientTickStart(ClientTickEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        if (!shouldSnap(player)) return;
        snapToCamera(player, false);
    }

    // LOWEST so attack-direction override applies after vanilla input
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof LocalPlayer player)) return;
        if (player != Minecraft.getInstance().player) return;
        if (!shouldSnap(player)) {
            hadAttackO = false;
            camYawO = Float.NaN;
            return;
        }
        snapToCamera(player, true);
    }

    private static boolean shouldSnap(LocalPlayer player) {
        if (Minecraft.getInstance().options.getCameraType() != CameraType.THIRD_PERSON_BACK) return false;
        if (isControllingMobMount(player)) return false;
        if (EpicFightHelper.isAttacking(player) || EpicFightHelper.isAttackKeyActive() || EpicFightHelper.isHoldingSkill(player)) return false;
        return player.swinging || BetterCombatHelper.isAttackInProgress();
    }

    private static boolean isControllingMobMount(LocalPlayer player) {
        Entity v = player.getVehicle();
        return v instanceof Mob mob && mob.getControllingPassenger() == player;
    }

    private static void snapToCamera(LocalPlayer player, boolean isPostTick) {
        if (BetterCombatHelper.isAttackInProgress()) {
            float camYaw = ShoulderSurfingHelper.getCameraYaw();
            player.setYRot(camYaw);
            if (isPostTick) {
                float prev = hadAttackO && !Float.isNaN(camYawO) ? camYawO : camYaw;
                player.yBodyRotO = prev;
                player.yHeadRotO = prev;
                player.yBodyRot = camYaw;
                player.yHeadRot = camYaw;
                camYawO = camYaw;
                hadAttackO = true;
            } else {
                player.yBodyRot = camYaw;
                player.yHeadRot = camYaw;
            }
            return;
        }
        float yRot = player.getYRot();
        player.yBodyRot = yRot;
        player.yBodyRotO = yRot;
        player.yHeadRot = yRot;
        player.yHeadRotO = yRot;
        if (isPostTick) {
            hadAttackO = false;
            camYawO = Float.NaN;
        }
    }
}
