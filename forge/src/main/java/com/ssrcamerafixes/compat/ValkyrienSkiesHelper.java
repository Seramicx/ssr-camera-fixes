package com.ssrcamerafixes.compat;

import com.mojang.logging.LogUtils;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.lang.reflect.Method;

public final class ValkyrienSkiesHelper {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static Method getShipMountedToMethod = null;
    private static Method getRenderTransformMethod = null;
    private static Method getTransformMethod = null;
    private static Method transformDirShipToWorldMethod = null;
    private static Method transformDirWorldToShipMethod = null;
    private static Method clipIncludeShipsMethod = null;
    private static Method vector3dX = null;
    private static Method vector3dY = null;
    private static Method vector3dZ = null;
    private static Class<?> vector3dClass = null;
    private static boolean resolved = false;
    private static boolean resolvedOk = false;
    private static boolean transformResolved = false;
    private static boolean transformOk = false;

    private ValkyrienSkiesHelper() {}

    private static void resolve() {
        if (resolved) return;
        resolved = true;
        try {
            Class<?> gameUtilsKt = Class.forName("org.valkyrienskies.mod.common.VSGameUtilsKt");
            getShipMountedToMethod = gameUtilsKt.getMethod("getShipMountedTo", Entity.class);
            resolvedOk = true;
        } catch (Throwable t) {
            LOGGER.debug("VS2 reflection unavailable, ship checks are a no-op: {}", t.getMessage());
        }
    }

    private static void resolveTransform() {
        if (transformResolved) return;
        transformResolved = true;
        resolve();
        if (!resolvedOk) return;
        try {
            Class<?> clientShip = Class.forName("org.valkyrienskies.core.api.ships.ClientShip");
            Class<?> ship = Class.forName("org.valkyrienskies.core.api.ships.Ship");
            Class<?> shipTransform = Class.forName("org.valkyrienskies.core.api.ships.properties.ShipTransform");
            vector3dClass = Class.forName("org.joml.Vector3d");
            Class<?> vector3dc = Class.forName("org.joml.Vector3dc");

            getRenderTransformMethod = clientShip.getMethod("getRenderTransform");
            getTransformMethod = ship.getMethod("getTransform");
            transformDirShipToWorldMethod = shipTransform.getMethod(
                    "transformDirectionNoScalingFromShipToWorld", vector3dc, vector3dClass);
            transformDirWorldToShipMethod = shipTransform.getMethod(
                    "transformDirectionNoScalingFromWorldToShip", vector3dc, vector3dClass);
            try {
                Class<?> raycastUtils = Class.forName("org.valkyrienskies.mod.common.world.RaycastUtilsKt");
                Class<?> clipContext = Class.forName("net.minecraft.world.level.ClipContext");
                Class<?> level = Class.forName("net.minecraft.world.level.Level");
                clipIncludeShipsMethod = raycastUtils.getMethod("clipIncludeShips", level, clipContext);
            } catch (Throwable t) {
                clipIncludeShipsMethod = null;
                LOGGER.debug("VS clipIncludeShips unavailable: {}", t.toString());
            }
            vector3dX = vector3dClass.getMethod("x");
            vector3dY = vector3dClass.getMethod("y");
            vector3dZ = vector3dClass.getMethod("z");
            transformOk = true;
        } catch (Throwable t) {
            LOGGER.debug("VS2 transform reflection unavailable: {}", t.toString());
        }
    }

    public static boolean isMountedOnShip(Entity entity) {
        if (entity == null) return false;
        resolve();
        if (!resolvedOk) return false;
        try {
            return getShipMountedToMethod.invoke(null, entity) != null;
        } catch (Throwable t) {
            return false;
        }
    }

    private static Object shipFor(Entity entity) {
        resolve();
        if (!resolvedOk || entity == null) return null;
        try {
            return getShipMountedToMethod.invoke(null, entity);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Object transformFor(Object ship) {
        resolveTransform();
        if (!transformOk || ship == null) return null;
        try {
            if (getRenderTransformMethod.getDeclaringClass().isInstance(ship)) {
                Object render = getRenderTransformMethod.invoke(ship);
                if (render != null) return render;
            }
            return getTransformMethod.invoke(ship);
        } catch (Throwable t) {
            return null;
        }
    }

    // Player yaw on a ship is ship-local; CGM/TACZ feed that into directionFromRotation and treat the
    // result as a world-space velocity. Rotate ship→world so the bullet matches the crosshair after the
    // ship yaws (when the ship is aligned local≈world so it looked fine by luck).
    public static Vec3 shipToWorldDirection(Entity rider, Vec3 shipLocalDir) {
        return transformDirection(rider, shipLocalDir, true);
    }

    // Inverse of shipToWorldDirection. Needed so a WORLD look (eye→crosshair hit) can be written
    // back into ship-local yRot/xRot. Do NOT use this on positions — directions only.
    public static Vec3 worldToShipDirection(Entity rider, Vec3 worldDir) {
        return transformDirection(rider, worldDir, false);
    }

    private static Vec3 transformDirection(Entity rider, Vec3 dir, boolean shipToWorld) {
        if (rider == null || dir == null) return null;
        resolveTransform();
        if (!transformOk) return null;

        Object transform = transformFor(shipFor(rider));
        if (transform == null) return null;

        Method method = shipToWorld ? transformDirShipToWorldMethod : transformDirWorldToShipMethod;
        if (method == null) return null;

        try {
            Object in = vector3dClass.getConstructor(double.class, double.class, double.class)
                    .newInstance(dir.x, dir.y, dir.z);
            Object out = vector3dClass.getConstructor().newInstance();
            method.invoke(transform, in, out);

            double x = ((Number) vector3dX.invoke(out)).doubleValue();
            double y = ((Number) vector3dY.invoke(out)).doubleValue();
            double z = ((Number) vector3dZ.invoke(out)).doubleValue();
            return new Vec3(x, y, z);
        } catch (Throwable t) {
            LOGGER.debug("{} dir failed: {}", shipToWorld ? "ship→world" : "world→ship", t.toString());
            return null;
        }
    }

    public static float[] shipLocalRotToWorldRot(Entity rider, float shipYaw, float shipPitch) {
        Vec3 local = Vec3.directionFromRotation(shipPitch, shipYaw);
        Vec3 world = shipToWorldDirection(rider, local);
        if (world == null) return null;
        double horiz = Math.sqrt(world.x * world.x + world.z * world.z);
        if (horiz < 1.0E-8 && Math.abs(world.y) < 1.0E-8) return null;
        float yaw = (float) (Mth.atan2(-world.x, world.z) * (double) Mth.RAD_TO_DEG);
        float pitch = (float) (Mth.atan2(-world.y, horiz) * (double) Mth.RAD_TO_DEG);
        return new float[]{yaw, Mth.clamp(pitch, -90.0F, 90.0F)};
    }

    // World look direction → ship-local yaw/pitch suitable for player.setYRot/setXRot on a mount.
    public static float[] worldDirToShipLocalRot(Entity rider, Vec3 worldDir) {
        Vec3 shipDir = worldToShipDirection(rider, worldDir);
        if (shipDir == null) return null;
        double horiz = Math.sqrt(shipDir.x * shipDir.x + shipDir.z * shipDir.z);
        if (horiz < 1.0E-8 && Math.abs(shipDir.y) < 1.0E-8) return null;
        float yaw = (float) (Mth.atan2(-shipDir.x, shipDir.z) * (double) Mth.RAD_TO_DEG);
        float pitch = (float) (Mth.atan2(-shipDir.y, horiz) * (double) Mth.RAD_TO_DEG);
        return new float[]{yaw, Mth.clamp(pitch, -90.0F, 90.0F)};
    }

    // VS-aware block clip (world + shipyard). Returns null if VS raycast is unavailable.
    public static net.minecraft.world.phys.BlockHitResult clipIncludeShips(
            net.minecraft.world.level.Level level, net.minecraft.world.level.ClipContext context) {
        if (level == null || context == null) return null;
        resolveTransform();
        Method m = clipIncludeShipsMethod;
        if (m == null) return null;
        try {
            return (net.minecraft.world.phys.BlockHitResult) m.invoke(null, level, context);
        } catch (Throwable t) {
            LOGGER.debug("clipIncludeShips failed: {}", t.toString());
            return null;
        }
    }
}
