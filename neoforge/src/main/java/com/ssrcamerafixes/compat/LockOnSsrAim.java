package com.ssrcamerafixes.compat;

// Remembers the last SSR-aware lock-on facing from EF lockOnTick so unlock continues free-look
// from that view instead of snapping to the player's body look
public final class LockOnSsrAim {

    private static float lastPitch;
    private static float lastYaw;
    private static boolean hasLastFacing;

    private LockOnSsrAim() {}

    public static void rememberFacing(float pitch, float yaw) {
        lastPitch = pitch;
        lastYaw = yaw;
        hasLastFacing = true;
    }

    public static boolean applyLastFacingToSsr() {
        if (!hasLastFacing) return false;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return false;
        ShoulderSurfingHelper.setCameraRotation(lastYaw, lastPitch);
        return true;
    }

    public static boolean hasLastFacing() {
        return hasLastFacing;
    }

    public static float lastPitch() {
        return lastPitch;
    }

    public static float lastYaw() {
        return lastYaw;
    }
}
