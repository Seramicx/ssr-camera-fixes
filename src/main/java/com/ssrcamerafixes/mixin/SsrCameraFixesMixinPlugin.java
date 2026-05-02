package com.ssrcamerafixes.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class SsrCameraFixesMixinPlugin implements IMixinConfigPlugin {

    private boolean hasEpicFight = false;

    @Override
    public void onLoad(String mixinPackage) {
        hasEpicFight = this.getClass().getClassLoader().getResource("yesman/epicfight/api/client/camera/EpicFightCameraAPI.class") != null;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains("MixinDisableEpicFightSsrLockOnTick") || mixinClassName.contains("MixinForceSsrOffsetDuringLockOn")) {
            return hasEpicFight;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
