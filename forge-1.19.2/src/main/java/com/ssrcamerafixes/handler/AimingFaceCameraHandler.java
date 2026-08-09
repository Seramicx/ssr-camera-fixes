package com.ssrcamerafixes.handler;

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
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class AimingFaceCameraHandler {

    public static final AimingFaceCameraHandler INSTANCE = new AimingFaceCameraHandler();

    private static boolean wasTaczAimingOrFiring = false;
    private static boolean wasSpellCastDown = false;

    private AimingFaceCameraHandler() {}

    // LOWEST so aiming-face-camera override wins over input handlers.
    // Mob mounts stay gated here: Better Mount Steering owns horse yaw; combat re-aim is tick-end below.
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
        boolean spell = IronSpellsHelper.isCasting() || IronSpellsHelper.anyCastKeymapDown()
                || EpicFightHelper.isCastLatchActive();
        if (!shield && !tacz && !spell) return;

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

    // Must run while mounted: BMS combat turns the horse to camera yaw, then this snaps player aim to the
    // shoulder-compensated crosshair and syncs Rot so Firebolt / gun packets see the right facing.
    @SubscribeEvent(priority = EventPriority.LOWEST)
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
        boolean ongoing = IronSpellsHelper.isCasting() || EpicFightHelper.isCastLatchActive();
        if (!pressEdge && !ongoing) return;

        faceCrosshairAndSync(mc, player);
    }

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
            faceCrosshairAndSync(mc, player);
        }
        wasTaczAimingOrFiring = tacz;
    }

    // After BMS end-of-tick combat reassert: keep player facing the SSR crosshair while casting.
    // On a mob mount, do NOT re-aim for guns/TaCZ here — BMS snaps rider+mount to camera yaw every tick, and
    // fighting that with lookAtCrosshair causes the camera to pulse in/out. CGM shots aim at fire() instead.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onClientTickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;

        boolean spell = IronSpellsHelper.isCasting()
                || IronSpellsHelper.anyCastKeymapDown()
                || EpicFightHelper.isCastLatchActive();
        if (isControllingMobMount(player)) {
            if (spell) {
                faceCrosshairAndSync(mc, player);
            }
            return;
        }

        if (spell
                || TaczHelper.isAimingOrFiring()
                || GunModHelper.isGunFiring()) {
            faceCrosshairAndSync(mc, player);
        }
    }

    public static void faceCrosshair(LocalPlayer player) {
        ShoulderSurfingHelper.lookAtCrosshairTarget();
        float yaw = player.getYRot();
        player.yBodyRot = yaw;
        player.yHeadRot = yaw;
    }

    public static void faceCrosshairAndSync(Minecraft mc, LocalPlayer player) {
        faceCrosshair(player);
        ClientPacketListener conn = mc.getConnection();
        if (conn != null) {
            conn.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.isOnGround()));
        }
    }

    // Mounted close-range fix for guns: raycast from camera while ignoring own mount, then turn player toward
    // that point from player eyes. This keeps close shots usable without reintroducing per-tick yaw fighting.
    public static void faceMountedGunTargetAndSync(Minecraft mc, LocalPlayer player) {
        Entity vehicle = player.getVehicle();
        if (!(vehicle instanceof Mob mob) || mob.getControllingPassenger() != player) {
            faceCrosshairAndSync(mc, player);
            return;
        }

        Vec3 camPos = mc.gameRenderer.getMainCamera().getPosition();
        com.mojang.math.Vector3f lookV = mc.gameRenderer.getMainCamera().getLookVector();
        if (camPos == null || lookV == null) {
            faceCrosshairAndSync(mc, player);
            return;
        }
        Vec3 look = new Vec3(lookV.x(), lookV.y(), lookV.z());
        Vec3 target = null;

        // First, trust Minecraft's current crosshair pick (entity/block) unless it's self or own mount.
        HitResult currentHit = mc.hitResult;
        if (currentHit instanceof EntityHitResult ehr) {
            Entity e = ehr.getEntity();
            if (e != null && e != player && e != vehicle) {
                target = ehr.getLocation();
            }
        } else if (currentHit instanceof BlockHitResult bhr && bhr.getType() != HitResult.Type.MISS) {
            target = bhr.getLocation();
        }

        // Fallback: custom camera ray that ignores own mount/self.
        if (target == null) {
            double range = 128.0D;
            Vec3 end = camPos.add(look.scale(range));
            BlockHitResult blockHit = player.level.clip(new ClipContext(
                    camPos, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

            double maxDistSq = range * range;
            if (blockHit.getType() != HitResult.Type.MISS) {
                maxDistSq = blockHit.getLocation().distanceToSqr(camPos);
            }

            AABB searchBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D, 1.0D, 1.0D);
            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                    player,
                    camPos,
                    camPos.add(look.scale(Math.sqrt(maxDistSq))),
                    searchBox,
                    entity -> entity != null && entity.isPickable() && entity != player && entity != vehicle,
                    maxDistSq
            );

            target = entityHit != null ? entityHit.getLocation()
                    : (blockHit.getType() != HitResult.Type.MISS ? blockHit.getLocation() : end);
        }

        Vec3 eye = player.getEyePosition();
        Vec3 delta = target.subtract(eye);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        if (horizontal < 1.0E-6 && Math.abs(delta.y) < 1.0E-6) {
            faceCrosshairAndSync(mc, player);
            return;
        }

        float yaw = (float) (Mth.atan2(delta.z, delta.x) * (180.0D / Math.PI)) - 90.0F;
        float pitch = (float) (-(Mth.atan2(delta.y, horizontal) * (180.0D / Math.PI)));
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.yBodyRot = yaw;
        player.yHeadRot = yaw;

        ClientPacketListener conn = mc.getConnection();
        if (conn != null) {
            conn.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.isOnGround()));
        }
    }
}
