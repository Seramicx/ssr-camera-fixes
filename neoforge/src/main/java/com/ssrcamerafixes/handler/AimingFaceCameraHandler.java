package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.ConfluenceHelper;
import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.GunModHelper;
import com.ssrcamerafixes.compat.IronSpellsHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.compat.TaczHelper;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

public final class AimingFaceCameraHandler {

    public static final AimingFaceCameraHandler INSTANCE = new AimingFaceCameraHandler();

    private static boolean wasSpellCastDown = false;
    private static boolean wasTaczAimingOrFiring = false;

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
        if (isControllingMobMount(player)) return;

        boolean shield = player.isBlocking();
        boolean tacz = TaczHelper.isAimingOrFiring();
        boolean confluenceGun = ConfluenceHelper.isGunFiringOrAiming(player);
        boolean spell = IronSpellsHelper.isCasting() || IronSpellsHelper.anyCastKeymapDown();
        if (!shield && !tacz && !confluenceGun && !spell) return;

        float camYaw = ShoulderSurfingHelper.getCameraYaw();
        if (shield) {
            player.setYRot(camYaw);
        }
        float bodyYaw = spell ? player.getYRot() : camYaw;
        player.yBodyRot = bodyYaw;
        player.yHeadRot = bodyYaw;
    }

    private static boolean isControllingMobMount(LocalPlayer player) {
        Entity v = player.getVehicle();
        return v instanceof Mob mob && mob.getControllingPassenger() == player;
    }

    // HIGHEST so aiming state is captured before downstream handlers
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onSpellCastTickStart(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) { wasSpellCastDown = false; return; }
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) { wasSpellCastDown = false; return; }
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) { wasSpellCastDown = false; return; }
        if (EpicFightHelper.isLockOnTargeting()) { wasSpellCastDown = false; return; }
        if (isControllingMobMount(player)) { wasSpellCastDown = false; return; }

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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onTaczTickStart(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null
                || mc.options.getCameraType() == CameraType.FIRST_PERSON
                || !ShoulderSurfingHelper.isShoulderSurfingActive()
                || isControllingMobMount(player)) {
            wasTaczAimingOrFiring = false;
            return;
        }

        boolean tacz = TaczHelper.isAimingOrFiring();
        if (tacz && !wasTaczAimingOrFiring) {
            faceCrosshairAndSync(mc, player);
        }
        wasTaczAimingOrFiring = tacz;

        // TerraGuns fires on ClientTickEvent.Post with a rotation-less packet, so the shot uses whatever yaw is
        // synced. While decoupled the body doesn't track the camera, so re-aim every tick the gun is up (not just
        // the press edge) or moving the camera between held shots leaves the bullet at the old offset yaw
        if (ConfluenceHelper.isGunFiringOrAiming(player)) {
            faceCrosshairAndSync(mc, player);
        }
    }

    // Irons casts and TerraGuns shots both send rotation-less packets on ClientTickEvent.Post, and the server reads
    // the player yaw when the spell/bullet resolves. Mounted, Better Mount Steering also rewrites the yaw to the
    // camera angle at tick end. Re-aim here at Post/LOWEST, last of all, so the crosshair yaw is what the server
    // sees whether or not the rider is on a mount
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onClientTickEnd(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;

        if (TaczHelper.isAimingOrFiring()
                || IronSpellsHelper.isCasting()
                || IronSpellsHelper.anyCastKeymapDown()
                || IronSpellsHelper.isCastLatchActive()
                || ConfluenceHelper.isGunFiringOrAiming(player)
                || (ConfluenceHelper.isHoldingManaWeapon(player) && player.isUsingItem())
                || GunModHelper.isGunFiring()) {
            faceCrosshairAndSync(mc, player);
        }
    }

    private static void faceCrosshairAndSync(Minecraft mc, LocalPlayer player) {
        ShoulderSurfingHelper.lookAtCrosshairTarget();
        ClientPacketListener conn = mc.getConnection();
        if (conn != null) {
            conn.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.onGround()));
        }
    }
}
