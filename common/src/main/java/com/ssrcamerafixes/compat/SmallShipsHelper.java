package com.ssrcamerafixes.compat;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

public final class SmallShipsHelper {

    private static Class<?> shipClass;
    private static boolean resolved = false;

    private SmallShipsHelper() {}

    public static boolean isShip(@Nullable Entity entity) {
        if (!resolved) {
            resolved = true;
            if (CompatSwitches.enabled("smallships")) {
                try {
                    shipClass = Class.forName("com.talhanation.smallships.world.entity.ship.Ship");
                } catch (Throwable ignored) {
                }
            }
        }
        return shipClass != null && shipClass.isInstance(entity);
    }
}
