package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.ConfluenceHelper;
import com.ssrcamerafixes.compat.EpicFightHelper;
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
    private static boolean wasConfluenceGunFiring = false;
    private static boolean wasManaUseDown = false;

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
            wasConfluenceGunFiring = false;
            wasManaUseDown = false;
            return;
        }

        boolean tacz = TaczHelper.isAimingOrFiring();
        if (tacz && !wasTaczAimingOrFiring) {
            faceCrosshairAndSync(mc, player);
        }
        wasTaczAimingOrFiring = tacz;

        boolean confluenceGun = ConfluenceHelper.isGunFiringOrAiming(player);
        if (confluenceGun && !wasConfluenceGunFiring) {
            faceCrosshairAndSync(mc, player);
        }
        wasConfluenceGunFiring = confluenceGun;

        // Confluence mana weapons fire from player rotation the instant use() runs, so align on the click edge
        boolean manaUse = mc.options.keyUse.isDown() && ConfluenceHelper.isHoldingManaWeapon(player);
        if (manaUse && !wasManaUseDown) {
            faceCrosshairAndSync(mc, player);
        }
        wasManaUseDown = manaUse;
    }

    private static void faceCrosshairAndSync(Minecraft mc, LocalPlayer player) {
        ShoulderSurfingHelper.lookAtCrosshairTarget();
        ClientPacketListener conn = mc.getConnection();
        if (conn != null) {
            conn.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.onGround()));
        }
    }
}
