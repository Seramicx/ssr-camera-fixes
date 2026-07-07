package com.ssrcamerafixes.compat;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public final class SpellEngineTargetHelper {

    private SpellEngineTargetHelper() {}

    public static boolean shouldUseMountedCameraRay(Entity caster) {
        if (!(caster instanceof LocalPlayer player)) return false;
        if (player != Minecraft.getInstance().player) return false;
        if (player.getVehicle() == null) return false;
        return ShoulderSurfingHelper.isShoulderSurfingActive();
    }

    public static Entity targetFromMountedCameraRay(Entity caster, float range, Predicate<Entity> predicate) {
        Minecraft mc = Minecraft.getInstance();
        Camera camera = mc.gameRenderer.getMainCamera();
        float yaw = ShoulderSurfingHelper.getCameraYaw();
        float pitch = ShoulderSurfingHelper.getCameraXRot();
        Vec3 start = camera.getPosition();
        Vec3 end = start.add(Vec3.directionFromRotation(pitch, yaw).scale(range));
        AABB search = caster.getBoundingBox().inflate(range, range, range);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(
                caster, start, end, search,
                candidate -> !candidate.isSpectator() && candidate.isPickable() && predicate.test(candidate),
                range * range);
        if (hit == null) return null;
        Vec3 hitPos = hit.getLocation();
        if (hitPos != null && !raycastObstacleFree(caster.level(), caster, start, hitPos)) {
            return null;
        }
        return hit.getEntity();
    }

    private static boolean raycastObstacleFree(Level level, Entity entity, Vec3 start, Vec3 end) {
        BlockHitResult block = level.clip(new ClipContext(
                start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity));
        return block.getType() == HitResult.Type.MISS;
    }
}
