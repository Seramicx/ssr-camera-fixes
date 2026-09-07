package com.ssrcamerafixes.compat;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;

public final class HeldCrossbowAim {

    private HeldCrossbowAim() {}

    // SSR's default adaptive_crosshair_hold_item_properties contains "minecraft:charged", and it matches on
    // the property being registered rather than on its value, so *uncharged* crossbows falsely count as aiming.
    // Only suppress that false positive. Charged holds keep aiming=true so adaptive crosshair stays on.
    public static boolean isPassiveHold(Entity entity) {
        if (!(entity instanceof LivingEntity living) || living.isUsingItem()) return false;
        if (!canReadHands(living)) return false;
        boolean anyCrossbow = false;
        boolean anyCharged = false;
        if (isCrossbow(living.getMainHandItem())) {
            anyCrossbow = true;
            if (CrossbowItem.isCharged(living.getMainHandItem())) anyCharged = true;
        }
        if (isCrossbow(living.getOffhandItem())) {
            anyCrossbow = true;
            if (CrossbowItem.isCharged(living.getOffhandItem())) anyCharged = true;
        }
        return anyCrossbow && !anyCharged;
    }

    // Charged crossbow held, not currently loading (isUsingItem). Fire is a click so isUsingItem is false here.
    public static boolean isChargedPassiveHold(Entity entity) {
        if (!(entity instanceof LivingEntity living) || living.isUsingItem()) return false;
        if (!canReadHands(living)) return false;
        return isChargedCrossbow(living.getMainHandItem()) || isChargedCrossbow(living.getOffhandItem());
    }

    // True when a charged crossbow is held (includes mid-load if somehow charged — fire-time snap uses this).
    public static boolean isChargedHold(Entity entity) {
        if (!(entity instanceof LivingEntity living)) return false;
        if (!canReadHands(living)) return false;
        return isChargedCrossbow(living.getMainHandItem()) || isChargedCrossbow(living.getOffhandItem());
    }

    // World-join can call into hand checks before Player.inventory exists (Better Combat etc.).
    private static boolean canReadHands(LivingEntity living) {
        if (!(living instanceof Player player)) return true;
        try {
            return player.getInventory() != null;
        } catch (Throwable t) {
            return false;
        }
    }

    private static boolean isCrossbow(ItemStack stack) {
        return stack != null && stack.getItem() instanceof CrossbowItem;
    }

    private static boolean isChargedCrossbow(ItemStack stack) {
        return isCrossbow(stack) && CrossbowItem.isCharged(stack);
    }
}
