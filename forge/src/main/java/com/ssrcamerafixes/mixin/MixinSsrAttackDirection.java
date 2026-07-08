package com.ssrcamerafixes.mixin;



import com.ssrcamerafixes.compat.EpicFightHelper;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;

import net.minecraft.client.player.LocalPlayer;

import net.minecraft.network.FriendlyByteBuf;

import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.Pseudo;

import org.spongepowered.asm.mixin.injection.At;

import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import yesman.epicfight.client.events.engine.ControlEngine;

import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

import yesman.epicfight.skill.BasicAttack;

import yesman.epicfight.skill.Skill;

import yesman.epicfight.skill.SkillContainer;

import yesman.epicfight.skill.weaponinnate.WeaponInnateSkill;



@Pseudo

@Mixin(value = Skill.class, remap = false, priority = 1100)

public abstract class MixinSsrAttackDirection {



    @Inject(method = "gatherArguments", at = @At("HEAD"), remap = false)

    private void ssrcamerafixes$alignYawToCameraOnAttack(

        SkillContainer container, ControlEngine controlEngine,

        CallbackInfoReturnable<FriendlyByteBuf> cir

    ) {

        if (!((Object) this instanceof BasicAttack) && !((Object) this instanceof WeaponInnateSkill)) return;

        if (!ShoulderSurfingHelper.isShoulderSurfingActive()) return;



        LocalPlayerPatch executer;

        try {

            executer = container.getClientExecutor();

        } catch (Exception e) {

            return;

        }

        if (executer == null) return;



        LocalPlayer player = executer.getOriginal();

        if (player == null) return;



        EpicFightHelper.signalAttack();

        ShoulderSurfingHelper.lookAtCrosshairTarget();

    }

}

