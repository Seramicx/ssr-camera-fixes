package com.ssrcamerafixes.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class SsrCameraFixesMixinPlugin implements IMixinConfigPlugin {

    private boolean hasEpicFight = false;
    private boolean hasTacz = false;
    private boolean hasRadialAggro = false;
    private boolean hasValkyrienSkies = false;
    private boolean hasFocus = false;

    @Override
    public void onLoad(String mixinPackage) {
        ClassLoader loader = this.getClass().getClassLoader();
        hasEpicFight = loader.getResource("yesman/epicfight/client/world/capabilites/entitypatch/player/LocalPlayerPatch.class") != null;
        hasTacz = loader.getResource("com/tacz/guns/compat/shouldersurfing/ShoulderSurfingPlugin.class") != null;
        hasRadialAggro = loader.getResource("com/mrbysco/radialaggroindicator/client/HudHandler.class") != null;
        hasValkyrienSkies = loader.getResource("org/valkyrienskies/mod/common/VSGameUtilsKt.class") != null;
        hasFocus = loader.getResource("com/jvn/focus/client/hud/VanillaCrosshairSuppressor.class") != null;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains("MixinDisableEpicFightSsrLockOnTick") || mixinClassName.contains("MixinForceSsrOffsetDuringLockOn") || mixinClassName.contains("MixinEpicFightForwardRotation") || mixinClassName.contains("MixinEpicFightModelYRotSnap") || mixinClassName.contains("MixinPhantomAscent") || mixinClassName.contains("MixinSyncLockOnCamera") || mixinClassName.contains("MixinSsrDodgeDirection") || mixinClassName.contains("MixinSsrAttackDirection") || mixinClassName.contains("MixinSuppressMovingInputYawFollow")) {
            return hasEpicFight;
        }
        if (mixinClassName.contains("MixinTaczGunHoldDecouple")) {
            return hasTacz;
        }
        if (mixinClassName.contains("MixinRadialAggroCameraYaw")) {
            return hasRadialAggro;
        }
        if (mixinClassName.contains("MixinSyncPlayerToSsrOnVsShip") || mixinClassName.contains("MixinSuppressSsrVsIncompatWarning") || mixinClassName.contains("MixinSsrLevelRendererForVs") || mixinClassName.contains("MixinSkipDynamicOffsetOnVsShip") || mixinClassName.contains("CameraMoveInvoker")) {
            return hasValkyrienSkies;
        }
        if (mixinClassName.contains("MixinFocusKeepCrosshairForSsr") || mixinClassName.contains("MixinFocusPickOnlyWhenLockedOn")) {
            return hasFocus;
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
