package com.ssrcamerafixes.compat;

import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

// 1.19.2 CGM / Scorched Guns (CGM addon). Mounted state gate for the gun arm re-pose (shotPitch feeds the
// weapon pose pitch); the shot itself aims via SSR lookAtCrosshairTarget, same as unmounted.
public final class CgmGunAimHelper {

    private CgmGunAimHelper() {}

    private static boolean isControllingMobMount(Player player) {
        Entity v = player.getVehicle();
        return v instanceof Mob mob && mob.getControllingPassenger() == player;
    }

    public static boolean needsMountedShotFix(Player player) {
        if (!(player instanceof LocalPlayer)) return false;
        if (player != Minecraft.getInstance().player) return false;
        if (!isControllingMobMount(player)) return false;
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.getCameraType() == CameraType.FIRST_PERSON) return false;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return false;
        if (EpicFightHelper.isLockOnTargeting()) return false;
        return true;
    }

    public static float shotPitch() {
        return Mth.clamp(ShoulderSurfingHelper.getCameraXRot(), -90F, 90F);
    }
}
