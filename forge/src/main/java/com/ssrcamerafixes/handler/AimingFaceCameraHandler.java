package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.SsrCameraFixesMod;
import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.compat.TaczHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SsrCameraFixesMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class AimingFaceCameraHandler {

    private static final Minecraft MC = Minecraft.getInstance();

    private static boolean wasTaczAimingOrFiring = false;

    private AimingFaceCameraHandler() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onMovementInput(MovementInputUpdateEvent event) {
        LocalPlayer player = MC.player;
        if (player == null) return;

        if (EpicFightHelper.isLockOnTargeting()) return;
        if (MC.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;

        boolean shield = player.isBlocking();
        boolean tacz = TaczHelper.isAimingOrFiring();
        if (!shield && !tacz) return;

        float camYaw = ShoulderSurfingHelper.getCameraYaw();
        if (shield) {
            player.setYRot(camYaw);
        }
        player.yBodyRot = camYaw;
        player.yHeadRot = camYaw;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClientTickStart(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        LocalPlayer player = MC.player;
        if (player == null) {
            wasTaczAimingOrFiring = false;
            return;
        }
        if (MC.options.getCameraType() == CameraType.FIRST_PERSON) {
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
            ClientPacketListener conn = MC.getConnection();
            if (conn != null) {
                conn.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.onGround()));
            }
        }
        wasTaczAimingOrFiring = tacz;
    }
}
