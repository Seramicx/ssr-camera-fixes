package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.MovementDirection;
import yesman.epicfight.client.events.engine.ControlEngine;
import yesman.epicfight.client.input.InputUtils;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.dodge.DodgeSkill;

@Pseudo
@Mixin(value = DodgeSkill.class, remap = false, priority = 1100)
public abstract class MixinSsrDodgeDirection {

    @Inject(method = "gatherArguments", at = @At("HEAD"), cancellable = true, remap = false)
    private void ssrcamerafixes$cameraRelativeDodge(
        SkillContainer container, ControlEngine controlEngine, CompoundTag arguments,
        CallbackInfo ci
    ) {
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (EpicFightHelper.isLockOnTargeting()) return;

        LocalPlayerPatch patch;
        try {
            patch = container.getClientExecutor();
        } catch (Exception e) {
            return;
        }
        if (patch == null) return;

        LocalPlayer player = patch.getOriginal();
        if (player == null) return;

        float pulse = (float) player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.SNEAKING_SPEED);
        InputUtils.sneakingTick(player, false, pulse);

        MovementDirection movementDirection =
                MovementDirection.fromInputState(InputManager.getInputState(player));
        int vertic = movementDirection.vertical();
        int horizon = movementDirection.horizontal();
        if (vertic == 0 && horizon == 0) return;

        float yRot = ShoulderSurfingHelper.getCameraYaw();
        float degree = Mth.wrapDegrees(
                -(90 * horizon * (1 - Math.abs(vertic)) + 45 * vertic * horizon) + yRot);

        arguments.putInt("direction", vertic >= 0 ? 0 : 1);
        arguments.putFloat("yRot", degree);
        ci.cancel();
    }
}
