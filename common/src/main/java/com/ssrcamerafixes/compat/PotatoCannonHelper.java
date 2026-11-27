package com.ssrcamerafixes.compat;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class PotatoCannonHelper {

    private PotatoCannonHelper() {}

    private static boolean resolved;
    private static Class<?> potatoCannonItem;

    private static void resolve() {
        if (resolved) return;
        resolved = true;
        if (!CompatSwitches.enabled("create")) return;

        try {
            potatoCannonItem = Class.forName("com.simibubi.create.content.equipment.potatoCannon.PotatoCannonItem");
        } catch (Throwable ignored) {}
    }

    public static boolean isHoldingPotatoCannon(LivingEntity entity) {
        resolve();
        if (potatoCannonItem == null || entity == null) return false;
        return isCannon(entity.getItemInHand(InteractionHand.MAIN_HAND))
            || isCannon(entity.getItemInHand(InteractionHand.OFF_HAND));
    }

    private static boolean isCannon(ItemStack stack) {
        return potatoCannonItem.isInstance(stack.getItem());
    }
}
