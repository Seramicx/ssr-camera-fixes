package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.network.client.CPSkillRequest;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.skill.dodge.DodgeSkill;

@Mixin(value = DodgeSkill.class, remap = false, priority = 1100)
public abstract class MixinSsrDodgeDirection {

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

        Minecraft mc = Minecraft.getInstance();

        int forward = 0;
        if (mc.options.keyUp.isDown()) forward += 1;
        if (mc.options.keyDown.isDown()) forward -= 1;

        int strafe = 0;
        if (mc.options.keyLeft.isDown()) strafe += 1;
        if (mc.options.keyRight.isDown()) strafe -= 1;

        if (forward == 0 && strafe == 0) {
            float fi = player.input.forwardImpulse;
            float li = player.input.leftImpulse;
            if (Math.abs(fi) > 0.3F) forward = fi > 0 ? 1 : -1;
            if (Math.abs(li) > 0.3F) strafe  = li > 0 ? 1 : -1;
            if (forward == 0 && strafe == 0) return;
        }

        float offset = -(90.0F * strafe * (1 - Math.abs(forward))
                       + 45.0F * forward * strafe);
        float cameraYaw = mc.gameRenderer.getMainCamera().getYRot();
        float angle = Mth.wrapDegrees(offset + cameraYaw);

        int dodgeType = forward < 0 ? 1 : 0;

        CPSkillRequest packet = new CPSkillRequest(container.getSlot());
        packet.getBuffer().writeInt(dodgeType);
        packet.getBuffer().writeFloat(angle);

        cir.setReturnValue(packet);
    }
}
