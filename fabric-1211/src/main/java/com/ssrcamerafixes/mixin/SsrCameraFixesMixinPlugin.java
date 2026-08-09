package com.ssrcamerafixes.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class SsrCameraFixesMixinPlugin implements IMixinConfigPlugin {

    private boolean hasSsrV5 = false;

    @Override
    public void onLoad(String mixinPackage) {
        ClassLoader loader = this.getClass().getClassLoader();
        hasSsrV5 = loader.getResource("com/github/exopandora/shouldersurfing/api/event/IEventBus.class") != null;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("V5")) {
            return hasSsrV5;
        }
        if (mixinClassName.endsWith("V4")) {
            return !hasSsrV5;
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
