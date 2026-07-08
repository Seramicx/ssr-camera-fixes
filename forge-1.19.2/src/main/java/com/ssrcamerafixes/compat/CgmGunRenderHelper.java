package com.ssrcamerafixes.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;

/** Re-apply CGM weapon arm pose after CGM's riding skip (runs at setupAnim TAIL, priority 1100). */
public final class CgmGunRenderHelper {

    private static boolean resolved;
    private static Class<?> gunItemClass;
    private static Method getModifiedGun;
    private static Method getGeneral;
    private static Method getGripType;
    private static Method getHeldAnimation;
    private static Method applyPlayerModelRotation;
    private static Method aimingHandlerGet;
    private static Method getAimProgress;

    private CgmGunRenderHelper() {}

    private static void resolve() {
        if (resolved) return;
        resolved = true;
        try {
            gunItemClass = Class.forName("com.mrcrayfish.guns.item.GunItem");
            getModifiedGun = gunItemClass.getMethod("getModifiedGun", ItemStack.class);
            Class<?> gunClass = Class.forName("com.mrcrayfish.guns.common.Gun");
            getGeneral = gunClass.getMethod("getGeneral");
            Class<?> generalClass = Class.forName("com.mrcrayfish.guns.common.Gun$General");
            getGripType = generalClass.getMethod("getGripType");
            Class<?> gripTypeClass = Class.forName("com.mrcrayfish.guns.common.GripType");
            getHeldAnimation = gripTypeClass.getMethod("getHeldAnimation");
            Class<?> heldAnimClass = Class.forName("com.mrcrayfish.guns.client.render.IHeldAnimation");
            applyPlayerModelRotation = heldAnimClass.getMethod(
                    "applyPlayerModelRotation",
                    net.minecraft.world.entity.player.Player.class,
                    ModelPart.class,
                    ModelPart.class,
                    ModelPart.class,
                    InteractionHand.class,
                    float.class
            );
            Class<?> aimingHandlerClass = Class.forName("com.mrcrayfish.guns.client.handler.AimingHandler");
            aimingHandlerGet = aimingHandlerClass.getMethod("get");
            getAimProgress = aimingHandlerClass.getMethod(
                    "getAimProgress",
                    net.minecraft.world.entity.player.Player.class,
                    float.class
            );
        } catch (Throwable ignored) {}
    }

    public static void applyMountedWeaponArms(LocalPlayer player, PlayerModel<?> model, float limbSwing) {
        if (!player.isPassenger() || limbSwing != 0.0F) return;
        if (!CgmGunAimHelper.needsMountedShotFix(player)) return;
        if (!GunModHelper.isHoldingGun(player)) return;
        resolve();
        if (applyPlayerModelRotation == null) return;

        ItemStack held = player.getMainHandItem();
        float savedXRot = player.getXRot();
        float savedXRotO = player.xRotO;
        try {
            float camPitch = CgmGunAimHelper.shotPitch();
            player.setXRot(camPitch);
            player.xRotO = camPitch;

            Object gunItem = gunItemClass.cast(held.getItem());
            Object gun = getModifiedGun.invoke(gunItem, held);
            Object general = getGeneral.invoke(gun);
            Object gripType = getGripType.invoke(general);
            Object heldAnimation = getHeldAnimation.invoke(gripType);
            Object aimingHandler = aimingHandlerGet.invoke(null);
            float aimProgress = (float) getAimProgress.invoke(
                    aimingHandler, player, Minecraft.getInstance().getFrameTime());

            applyPlayerModelRotation.invoke(
                    heldAnimation,
                    player,
                    model.rightArm,
                    model.leftArm,
                    model.head,
                    InteractionHand.MAIN_HAND,
                    aimProgress
            );

            copyModelAngles(model.rightArm, model.rightSleeve);
            copyModelAngles(model.leftArm, model.leftSleeve);
            copyModelAngles(model.head, model.hat);
        } catch (Throwable ignored) {
        } finally {
            player.setXRot(savedXRot);
            player.xRotO = savedXRotO;
        }
    }

    private static void copyModelAngles(ModelPart source, ModelPart target) {
        target.xRot = source.xRot;
        target.yRot = source.yRot;
        target.zRot = source.zRot;
    }
}
