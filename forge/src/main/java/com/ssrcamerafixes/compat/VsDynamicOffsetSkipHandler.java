package com.ssrcamerafixes.compat;

import com.github.exopandora.shouldersurfing.api.client.event.ComputeTargetCameraOffsetEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

// SSR's dynamic offset raycast is world-space, but a VS ship's blocks live in a separate physics level, so on
// certain yaws the ray clips the hull and SSR pulls the shoulder in. record() runs just before SSR's dynamic
// offset handler (priority 2000) and restore() just after, so the raycast shrink is undone only while on a ship.
public final class VsDynamicOffsetSkipHandler {

    private Vec3 saved;

    public void record(ComputeTargetCameraOffsetEvent event) {
        this.saved = onShip() ? event.getResult() : null;
    }

    public void restore(ComputeTargetCameraOffsetEvent event) {
        if (this.saved != null) {
            event.setResult(this.saved);
            this.saved = null;
        }
    }

    private static boolean onShip() {
        LocalPlayer player = Minecraft.getInstance().player;
        return player != null && ValkyrienSkiesHelper.isMountedOnShip(player);
    }
}
