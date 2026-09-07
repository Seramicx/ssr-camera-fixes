package com.ssrcamerafixes.compat;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

public final class WizardryReduxHelper {

    private static boolean loaded;
    private static boolean loadedResolved;

    private static Class<?> castItemClass;
    private static Class<?> bombItemClass;
    private static boolean classesResolved;

    private WizardryReduxHelper() {}

    public static boolean isLoaded() {
        if (!loadedResolved) {
            loaded = ModList.get().isLoaded("ebwizardry");
            loadedResolved = true;
        }
        return loaded;
    }

    // Wands and scrolls share ICastItem; bombs are thrown straight out of Item#use from the player rotation
    public static boolean isCastItem(@Nullable Item item) {
        if (item == null || !isLoaded()) return false;
        resolveClasses();
        return (castItemClass != null && castItemClass.isInstance(item))
                || (bombItemClass != null && bombItemClass.isInstance(item));
    }

    // Charged and continuous spells resolve in onUseTick, so the aim has to hold for the whole hold
    public static boolean isCastActive(@Nullable Player player) {
        if (player == null || !isLoaded() || !player.isUsingItem()) return false;
        ItemStack stack = player.getUseItem();
        return !stack.isEmpty() && isCastItem(stack.getItem());
    }

    private static void resolveClasses() {
        if (classesResolved) return;
        classesResolved = true;
        try {
            castItemClass = Class.forName("com.binaris.wizardry.api.content.item.ICastItem");
        } catch (ClassNotFoundException ignored) {}
        try {
            bombItemClass = Class.forName("com.binaris.wizardry.content.item.BombItem");
        } catch (ClassNotFoundException ignored) {}
    }
}
