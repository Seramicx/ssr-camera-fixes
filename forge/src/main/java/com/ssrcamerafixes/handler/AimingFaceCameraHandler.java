package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.IronSpellsHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.compat.TaczHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class AimingFaceCameraHandler {

    public static final AimingFaceCameraHandler INSTANCE = new AimingFaceCameraHandler();

    private static boolean wasTaczAimingOrFiring = false;
    private static boolean wasSpellCastDown = false;

    private AimingFaceCameraHandler() {}

    // LOWEST so aiming-face-camera override wins over input handlers
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMovementInput(MovementInputUpdateEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;

        if (EpicFightHelper.isLockOnTargeting()) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;

        boolean shield = player.isBlocking();
        boolean tacz = TaczHelper.isAimingOrFiring();
        boolean spell = IronSpellsHelper.isCasting() || IronSpellsHelper.anyCastKeymapDown();
        if (!shield && !tacz && !spell) return;

        float camYaw = ShoulderSurfingHelper.getCameraYaw();
        if (shield) {
            player.setYRot(camYaw);
        }
        float bodyYaw = spell ? player.getYRot() : camYaw;
        player.yBodyRot = bodyYaw;
        player.yHeadRot = bodyYaw;
    }

    // HIGHEST so aiming state is captured before downstream handlers
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSpellCastTickStart(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) { wasSpellCastDown = false; return; }
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) { wasSpellCastDown = false; return; }
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) { wasSpellCastDown = false; return; }
        if (EpicFightHelper.isLockOnTargeting()) { wasSpellCastDown = false; return; }

        boolean nowDown = IronSpellsHelper.anyCastKeymapDown();
        boolean pressEdge = nowDown && !wasSpellCastDown;
        wasSpellCastDown = nowDown;
        boolean ongoing = IronSpellsHelper.isCasting();
        if (!pressEdge && !ongoing) return;

        ShoulderSurfingHelper.lookAtCrosshairTarget();
        ClientPacketListener conn = mc.getConnection();
        if (conn != null) {
            conn.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.onGround()));
        }
    }

    // LOWEST so aiming-face-camera override wins over input handlers
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onClientTickStart(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            wasTaczAimingOrFiring = false;
            return;
        }
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) {
            wasTaczAimingOrFiring = false;
            return;
        }
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) {
            wasTaczAimingOrFiring = false;
            return;
        }

        boolean tacz = TaczHelper.isAimingOrFiring();
        if (tacz && !wasTaczAimingOrFiring) {
            ShoulderSurfingHelper.lookAtCrosshairTarget();
            ClientPacketListener conn = mc.getConnection();
            if (conn != null) {
                conn.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.onGround()));
            }
        }
        wasTaczAimingOrFiring = tacz;
    }
}
