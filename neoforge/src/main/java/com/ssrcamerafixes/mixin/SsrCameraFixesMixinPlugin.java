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
    private boolean hasFocus = false;
    private boolean hasConfluence = false;
    private boolean hasIronsSpells = false;
    private boolean hasScorchedGuns = false;
    private boolean hasBetterMountSteering = false;

    @Override
    public void onLoad(String mixinPackage) {
        ClassLoader loader = this.getClass().getClassLoader();
        hasEpicFight = loader.getResource("yesman/epicfight/api/client/camera/EpicFightCameraAPI.class") != null;
        hasTacz = loader.getResource("com/tacz/guns/compat/shouldersurfing/ShoulderSurfingPlugin.class") != null;
        hasRadialAggro = loader.getResource("com/mrbysco/radialaggroindicator/client/HudHandler.class") != null;
        hasFocus = loader.getResource("com/jvn/focus/client/hud/VanillaCrosshairSuppressor.class") != null;
        hasConfluence = loader.getResource("org/confluence/mod/common/item/mana/ManaStaffItem.class") != null;
        hasIronsSpells = loader.getResource("io/redspace/ironsspellbooks/player/ClientMagicData.class") != null;
        hasScorchedGuns = loader.getResource("top/ribs/scguns/client/handler/ShootingHandler.class") != null;
        hasBetterMountSteering = loader.getResource("com/bettermountsteering/handler/MountSteeringHandler.class") != null;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.contains("MixinDisableEpicFightSsrLockOnTick") || mixinClassName.contains("MixinForceSsrOffsetDuringLockOn") || mixinClassName.contains("MixinEpicFightForwardRotation") || mixinClassName.contains("MixinEpicFightModelYRotSnap") || mixinClassName.contains("MixinPhantomAscentForwardRoll") || mixinClassName.contains("MixinSyncLockOnCamera") || mixinClassName.contains("MixinSsrDodgeDirection")) {
            return hasEpicFight;
        }
        if (mixinClassName.contains("MixinTaczGunHoldDecouple")) {
            return hasTacz;
        }
        if (mixinClassName.contains("MixinRadialAggroCameraYaw")) {
            return hasRadialAggro;
        }
        if (mixinClassName.contains("MixinFocusKeepCrosshairForSsr") || mixinClassName.contains("MixinFocusPickOnlyWhenLockedOn")) {
            return hasFocus;
        }
        if (mixinClassName.contains("MixinConfluenceWandFaceCamera") || mixinClassName.contains("MixinConfluenceGunFaceCamera")) {
            return hasConfluence;
        }
        if (mixinClassName.contains("MixinIronsInstantCastFaceCamera") || mixinClassName.contains("MixinIronsCastPacketFaceCamera") || mixinClassName.contains("MixinIronsQuickCastPacketFaceCamera")) {
            return hasIronsSpells;
        }
        if (mixinClassName.contains("MixinScorchedGunsFaceCamera")) {
            return hasScorchedGuns;
        }
        if (mixinClassName.contains("MixinSsrSuppressFollowDuringMountRotate") || mixinClassName.contains("MixinSsrSuppressTurnSnapback")) {
            return hasBetterMountSteering;
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
