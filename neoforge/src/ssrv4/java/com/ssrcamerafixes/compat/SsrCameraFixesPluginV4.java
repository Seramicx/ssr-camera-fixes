package com.ssrcamerafixes.compat;

import com.github.exopandora.shouldersurfing.api.callback.ICameraCouplingCallback;
import com.github.exopandora.shouldersurfing.api.callback.IPlayerInputCallback;
import com.github.exopandora.shouldersurfing.api.callback.IPlayerStateCallback;
import com.github.exopandora.shouldersurfing.api.callback.ITargetCameraOffsetCallback;
import com.github.exopandora.shouldersurfing.api.client.IShoulderSurfing;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingPlugin;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingRegistrar;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class SsrCameraFixesPluginV4 implements IShoulderSurfingPlugin {

    @Override
    public void register(IShoulderSurfingRegistrar registrar) {
        registrar.registerCameraCouplingCallback(new CouplingCallback());
        registrar.registerPlayerStateCallback(new AttackStateCallback());
        registrar.registerTargetCameraOffsetCallback(new OffsetCallback());
        registrar.registerPlayerInputCallback(new ForceVanillaInputCallback());
    }

    private static final class OffsetCallback implements ITargetCameraOffsetCallback {
        @Override
        public Vec3 pre(IShoulderSurfing instance, Vec3 targetOffset, Vec3 defaultOffset) {
            // Apply early so later SSR passes (looking-down center, dynamic adjust) see overhead intent.
            if (CameraFixCore.lockOnOffset(defaultOffset) != null) {
                return targetOffset;
            }
            return CameraFixCore.overheadOffset(targetOffset);
        }

        @Override
        public Vec3 post(IShoulderSurfing instance, Vec3 targetOffset, Vec3 defaultOffset) {
            Vec3 lockOn = CameraFixCore.lockOnOffset(defaultOffset);
            if (lockOn != null) {
                return lockOn;
            }
            // Re-apply after looking-down center may have zeroed x/y.
            return CameraFixCore.overheadOffset(targetOffset);
        }
    }

    private static final class CouplingCallback implements ICameraCouplingCallback {
        @Override
        public boolean isForcingCameraCoupling(Minecraft minecraft) {
            return CameraFixCore.needsCameraCoupling();
        }
    }

    private static final class AttackStateCallback implements IPlayerStateCallback {
        @Override
        public Result isAttacking(IsAttackingContext context) {
            return CameraFixCore.needsCameraCoupling() ? Result.TRUE : Result.PASS;
        }
    }

    private static final class ForceVanillaInputCallback implements IPlayerInputCallback {
        @Override
        public boolean isForcingVanillaMovementInput(IsForcingVanillaMovementInputContext context) {
            return CameraFixCore.forceVanillaInput();
        }
    }
}
