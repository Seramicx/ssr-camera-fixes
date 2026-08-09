package com.ssrcamerafixes.compat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

public final class HeldCrossbowAim {

    private HeldCrossbowAim() {}

    // SSR's default adaptive_crosshair_hold_item_properties contains "minecraft:charged", and it matches on
    // the property being registered rather than on its value, so every crossbow counts as aiming whether or
    // not it is loaded. Loading and firing still go through isUsingItem, so gating on that keeps those turning.
    public static boolean isPassiveHold(Entity entity) {
        if (!(entity instanceof LivingEntity living) || living.isUsingItem()) return false;
        return isCrossbow(living.getMainHandItem()) || isCrossbow(living.getOffhandItem());
    }

    private static boolean isCrossbow(ItemStack stack) {
        return stack != null && stack.getItem() instanceof CrossbowItem;
    }
}
