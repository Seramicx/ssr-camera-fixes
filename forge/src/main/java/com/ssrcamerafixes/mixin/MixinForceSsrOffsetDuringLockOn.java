package com.ssrcamerafixes.mixin;

import com.github.exopandora.shouldersurfing.client.ShoulderSurfingCamera;
import com.github.exopandora.shouldersurfing.config.Config;
import com.ssrcamerafixes.SsrCameraFixesConfig;
import com.ssrcamerafixes.handler.ShoulderCycleHandler;
import com.ssrcamerafixes.compat.EpicFightHelper;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Pseudo
@Mixin(value = ShoulderSurfingCamera.class, remap = false)
public abstract class MixinForceSsrOffsetDuringLockOn {

    @Shadow private Vec3 targetOffset;

    @Inject(
        method = "calcOffset",
        at = @At(
            value = "INVOKE",
            target = "Lcom/github/exopandora/shouldersurfing/client/ShoulderSurfingCamera;calcCameraDrag(Lnet/minecraft/client/Camera;Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/world/phys/Vec3;",
            shift = At.Shift.AFTER
        ),
        require = 0,
        remap = false
    )
    private void ssrcamerafixes$forceConfigOffsetDuringLockOn(
        Camera camera,
        BlockGetter level,
        float partialTick,
        Entity cameraEntity,
        CallbackInfoReturnable<Vec3> cir
    ) {
        if (!EpicFightHelper.isLockOnTargeting()) return;

        double offX = Config.CLIENT.getOffsetX();
        double offY = Config.CLIENT.getOffsetY();
        double offZ = Config.CLIENT.getOffsetZ();
        if (ShoulderCycleHandler.getMode() == ShoulderCycleHandler.Mode.OVERHEAD) {
            offX = 0.0;
            try {
                offY = SsrCameraFixesConfig.CAMERA_OVERHEAD_OFFSET_Y.get();
            } catch (Throwable t) {
                offY = 1.2;
            }
        }

        this.targetOffset = new Vec3(offX, offY, offZ);
    }
}
