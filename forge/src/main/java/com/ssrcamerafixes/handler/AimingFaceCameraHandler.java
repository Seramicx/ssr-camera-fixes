package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.BetterMountSteeringHelper;
import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.GunModHelper;
import com.ssrcamerafixes.compat.IronSpellsHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.compat.SuperbWarfareHelper;
import com.ssrcamerafixes.compat.TaczHelper;
import com.ssrcamerafixes.compat.ValkyrienSkiesHelper;
import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
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
import org.joml.Vector3f;

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
        if (isControllingMobMount(player)) return;

        boolean shield = player.isBlocking();
        boolean efHold = EpicFightHelper.isHoldingSkill(player);
        boolean efAttack = EpicFightHelper.isAttackKeyActive();
        boolean tacz = TaczHelper.isAimingOrFiring();
        boolean spell = IronSpellsHelper.isCasting() || IronSpellsHelper.anyCastKeymapDown();
        if (!shield && !efHold && !efAttack && !tacz && !spell) return;

        if (shield || efHold || efAttack) {
            ShoulderSurfingHelper.lookAtCrosshairTarget();
        }
        float bodyYaw = player.getYRot();
        player.yBodyRot = bodyYaw;
        player.yHeadRot = bodyYaw;
    }

    private static boolean isControllingMobMount(LocalPlayer player) {
        Entity v = player.getVehicle();
        return v instanceof Mob mob && mob.getControllingPassenger() == player;
    }

    // The render pass hands VS the camera's render rotation, so restore the crosshair target here or whatever fires
    // this tick shoots along a stale yaw depending on how render and tick interleaved.
    // Do not run during BMS mount-rotate — overwriting body yaw with camera yaw fights the ship turn and
    // jitters SSR offset against VS's camera rebase (v5).
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onVsShipTickStart(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (!ValkyrienSkiesHelper.isMountedOnShip(player)) return;
        if (BetterMountSteeringHelper.isMountRotateActive()
                || BetterMountSteeringHelper.isDecoupleTransitioning()) {
            return;
        }

        float ssrYaw = ShoulderSurfingHelper.getCameraYaw();
        float ssrPitch = ShoulderSurfingHelper.getCameraXRot();
        if (Float.isNaN(ssrYaw) || Float.isNaN(ssrPitch)) return;

        player.setYRot(ssrYaw);
        player.setXRot(ssrPitch);
        player.yRotO = ssrYaw;
        player.xRotO = ssrPitch;
        player.yBodyRot = ssrYaw;
        player.yBodyRotO = ssrYaw;
        player.yHeadRot = ssrYaw;
        player.yHeadRotO = ssrYaw;
    }

    // LOWEST so it runs after onVsShipTickStart writes the SSR camera rotation into the player, otherwise the
    // ship handler overwrites the crosshair yaw and the cast leaves with the shoulder offset
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
        boolean ongoing = IronSpellsHelper.isCasting() || IronSpellsHelper.isCastLatchActive();
        if (!pressEdge && !ongoing) return;

        faceCrosshairAndSync(mc, player);
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
            faceCrosshairAndSync(mc, player);
        }
        wasTaczAimingOrFiring = tacz;
    }

    // Epic Fight hold skills (guard) and attack animations need the crosshair yaw to win over decoupled movement
    // input each tick, especially midair while holding backward movement keys
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onClientTickEnd(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) return;
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;

        // Irons casts and Superb Warfare shots both send rotation-less packets and the server reads the player yaw
        // when the spell or bullet resolves, so this cannot be gated on the mount being a Mob or the correction
        // never runs on a Valkyrien Skies ship
        if (EpicFightHelper.isAttackKeyActive()
                || EpicFightHelper.isHoldingSkill(player)
                || TaczHelper.isAimingOrFiring()
                || IronSpellsHelper.isCasting()
                || IronSpellsHelper.anyCastKeymapDown()
                || IronSpellsHelper.isCastLatchActive()
                || SuperbWarfareHelper.isAimingOrFiring()
                || SuperbWarfareHelper.isFireLatchActive()
                || GunModHelper.isGunFiring()) {
            faceCrosshairAndSync(mc, player);
        }
    }

    // Off-ship: SSR pick→lookAt fixes shoulder parallax (reflective lookAtCrosshairTarget — v4 + v5).
    // On-ship: do NOT call lookAt / lookAtCrosshairTarget — that writes world euler into ship-local yRot.
    // VS spaces (mounted): camera pos/look + getEyePosition() are already WORLD; yRot/xRot stay ship-local.
    // Aim eyeW → real camera-ray hit (VS clipIncludeShips + entity pick), then worldToShip the direction
    // only. Never shipToWorld the eye. Fallback = camera-parallel copy. SSR-agnostic (v4 + v5).
    public static void faceCrosshair(LocalPlayer player) {
        if (!ValkyrienSkiesHelper.isMountedOnShip(player)) {
            ShoulderSurfingHelper.lookAtCrosshairTarget();
            return;
        }
        if (!faceThroughCrosshairOnShip(player)) {
            applyShipLocalCameraCopy(player);
        }
    }

    private static boolean faceThroughCrosshairOnShip(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        if (camera == null) return false;

        // Both already WORLD on a mount — do not transform them again.
        Vec3 eyeWorld = player.getEyePosition(1.0F);
        Vec3 camPos = camera.getPosition();
        Vector3f lookV = camera.getLookVector();
        if (eyeWorld == null || camPos == null || lookV == null) return false;

        Vec3 lookWorld = new Vec3(lookV.x(), lookV.y(), lookV.z());
        if (lookWorld.lengthSqr() < 1.0E-12) return false;
        lookWorld = lookWorld.normalize();

        // Fixed-range aim only zeroes the miss at that distance (64 under-corrected at typical ranges).
        // Pick the real crosshair hit along the camera ray so eye→hit matches what's under the reticle.
        final double range = 128.0D;
        Vec3 end = camPos.add(lookWorld.scale(range));
        Vec3 targetWorld = pickCameraRayTarget(player, camPos, end, lookWorld, range);

        Vec3 dirWorld = targetWorld.subtract(eyeWorld);
        if (dirWorld.lengthSqr() < 1.0E-12) return false;

        float[] rot = ValkyrienSkiesHelper.worldDirToShipLocalRot(player, dirWorld);
        if (rot == null) return false;

        applyShipLocalRot(player, rot[0], rot[1]);
        return true;
    }

    // World-space hit under the crosshair: VS-aware block clip + entity pick, else far point on the ray.
    private static Vec3 pickCameraRayTarget(LocalPlayer player, Vec3 camPos, Vec3 end,
                                            Vec3 lookWorld, double range) {
        Entity vehicle = player.getVehicle();
        double bestDistSq = range * range;
        Vec3 best = end;

        BlockHitResult blockHit = ValkyrienSkiesHelper.clipIncludeShips(
                player.level(),
                new ClipContext(camPos, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (blockHit == null) {
            try {
                blockHit = player.level().clip(new ClipContext(
                        camPos, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            } catch (Throwable ignored) {
                blockHit = null;
            }
        }
        if (blockHit != null && blockHit.getType() != HitResult.Type.MISS) {
            double d = blockHit.getLocation().distanceToSqr(camPos);
            if (d < bestDistSq) {
                bestDistSq = d;
                best = blockHit.getLocation();
            }
        }

        try {
            AABB search = player.getBoundingBox()
                    .expandTowards(lookWorld.scale(range))
                    .inflate(1.0D);
            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                    player,
                    camPos,
                    camPos.add(lookWorld.scale(Math.sqrt(bestDistSq))),
                    search,
                    e -> e != null && e.isPickable() && e != player && e != vehicle && !e.isSpectator(),
                    bestDistSq);
            if (entityHit != null) {
                best = entityHit.getLocation();
            }
        } catch (Throwable ignored) {}

        return best;
    }

    private static void applyShipLocalCameraCopy(LocalPlayer player) {
        float yaw = ShoulderSurfingHelper.getCameraYaw();
        float pitch = ShoulderSurfingHelper.getCameraXRot();
        if (Float.isNaN(yaw) || Float.isNaN(pitch)) return;
        applyShipLocalRot(player, yaw, pitch);
    }

    private static void applyShipLocalRot(LocalPlayer player, float yaw, float pitch) {
        player.setYRot(yaw);
        player.setXRot(pitch);
        player.yBodyRot = yaw;
        player.yHeadRot = yaw;
        ShoulderSurfingHelper.setLastMovedYRot(yaw);
    }

    public static void faceCrosshairAndSync(Minecraft mc, LocalPlayer player) {
        faceCrosshair(player);
        ClientPacketListener conn = mc.getConnection();
        if (conn != null) {
            conn.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.onGround()));
        }
    }
}
