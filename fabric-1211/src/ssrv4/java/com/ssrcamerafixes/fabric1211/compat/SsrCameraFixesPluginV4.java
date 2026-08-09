package com.ssrcamerafixes.fabric1211.compat;

import com.github.exopandora.shouldersurfing.api.callback.IPlayerInputCallback;
import com.github.exopandora.shouldersurfing.api.callback.ITargetCameraOffsetCallback;
import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingPlugin;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingRegistrar;
import net.minecraft.world.phys.Vec3;

public class SsrCameraFixesPluginV4 implements IShoulderSurfingPlugin {

    @Override
    public void register(IShoulderSurfingRegistrar registrar) {
        registrar.registerTargetCameraOffsetCallback(new OffsetCallback());
        registrar.registerPlayerInputCallback(new ForceVanillaInputCallback());
    }

    private static final class OffsetCallback implements ITargetCameraOffsetCallback {
        @Override
        public Vec3 pre(IShoulderSurfing instance, Vec3 targetOffset, Vec3 defaultOffset) {
            return CameraFixCore.overheadOffset(targetOffset);
        }

        @Override
        public Vec3 post(IShoulderSurfing instance, Vec3 targetOffset, Vec3 defaultOffset) {
            return CameraFixCore.overheadOffset(targetOffset);
        }
    }

    private static final class ForceVanillaInputCallback implements IPlayerInputCallback {
        @Override
        public boolean isForcingVanillaMovementInput(IsForcingVanillaMovementInputContext context) {
            return CameraFixCore.forceVanillaInput();
        }
    }
}
