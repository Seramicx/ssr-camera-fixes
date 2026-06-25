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
import yesman.epicfight.client.events.engine.ControllEngine;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;
import yesman.epicfight.skill.AirAttack;
import yesman.epicfight.skill.BasicAttack;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;

@Pseudo
@Mixin(value = Skill.class, remap = false, priority = 1100)
public abstract class MixinSsrAttackDirection {

    @Inject(method = "gatherArguments", at = @At("HEAD"), remap = false)
    private void ssrcamerafixes$alignYawToCameraOnAttack(
        LocalPlayerPatch executer, ControllEngine controllEngine,
        CallbackInfoReturnable<FriendlyByteBuf> cir
    ) {
        if (!((Object) this instanceof BasicAttack) && !((Object) this instanceof AirAttack)
                && !((Object) this instanceof WeaponInnateSkill)) return;
        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        if (executer == null) return;

        LocalPlayer player = executer.getOriginal();
        if (player == null) return;

        EpicFightHelper.signalAttack();

        float cameraYaw = Mth.wrapDegrees(ShoulderSurfingHelper.getCameraYaw());

        player.setYRot(cameraYaw);
        player.yRotO = cameraYaw;
        player.yBodyRot = cameraYaw;
        player.yBodyRotO = cameraYaw;
        player.yHeadRot = cameraYaw;
        player.yHeadRotO = cameraYaw;
    }
}
