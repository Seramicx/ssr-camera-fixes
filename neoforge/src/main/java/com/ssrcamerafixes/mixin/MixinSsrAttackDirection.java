package com.ssrcamerafixes.mixin;



import com.ssrcamerafixes.compat.EpicFightHelper;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;

import net.minecraft.client.player.LocalPlayer;

import net.minecraft.nbt.CompoundTag;

import org.spongepowered.asm.mixin.Mixin;

import org.spongepowered.asm.mixin.Pseudo;

import org.spongepowered.asm.mixin.injection.At;

import org.spongepowered.asm.mixin.injection.Inject;

import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import yesman.epicfight.client.events.engine.ControlEngine;

import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

import yesman.epicfight.skill.Skill;

import yesman.epicfight.skill.SkillCategories;

import yesman.epicfight.skill.SkillCategory;

import yesman.epicfight.skill.SkillContainer;



@Pseudo

@Mixin(value = Skill.class, remap = false, priority = 1100)

public abstract class MixinSsrAttackDirection {



    @Inject(method = "gatherArguments", at = @At("HEAD"), remap = false)

    private void ssrcamerafixes$alignYawToCameraOnAttack(

        SkillContainer container, ControlEngine controlEngine, CompoundTag arguments,

        CallbackInfo ci

    ) {

        SkillCategory category = ((Skill) (Object) this).getCategory();

        if (category != SkillCategories.BASIC_ATTACK && category != SkillCategories.WEAPON_INNATE) return;

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

