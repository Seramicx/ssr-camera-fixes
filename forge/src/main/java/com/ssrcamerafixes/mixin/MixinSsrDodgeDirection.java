package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.client.input.InputManager;
import yesman.epicfight.api.client.input.MovementDirection;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.client.CPSkillRequest;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.dodge.DodgeSkill;

@Pseudo
@Mixin(value = DodgeSkill.class, remap = false, priority = 1100)
public abstract class MixinSsrDodgeDirection {

    // EF DodgeSkill.getExecutionPacket: MovementDirection.fromInputState + getForwardYRot().
    // Under SSR v5 use camera yaw directly (same intent as MixinEpicFightForwardRotation).
    @Inject(method = "getExecutionPacket", at = @At("HEAD"), cancellable = true, remap = false)
    private void ssrcamerafixes$cameraRelativeDodge(
        SkillContainer container, FriendlyByteBuf originalBuf,
        CallbackInfoReturnable<Object> cir
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

        MovementDirection movementDirection =
                MovementDirection.fromInputState(InputManager.getInputState(player));
        int vertic = movementDirection.vertical();
        int horizon = movementDirection.horizontal();
        if (vertic == 0 && horizon == 0) return;

        float yRot = ShoulderSurfingHelper.getCameraYaw();
        float degree = Mth.wrapDegrees(
                -(90 * horizon * (1 - Math.abs(vertic)) + 45 * vertic * horizon) + yRot);

        CPSkillRequest packet = new CPSkillRequest(container.getSlot());
        packet.getBuffer().writeInt(vertic >= 0 ? 0 : 1);
        packet.getBuffer().writeFloat(degree);
        cir.setReturnValue(packet);
    }
}
