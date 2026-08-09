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
    private boolean hasBetterMountSteering = false;
    private boolean hasScorchedGuns = false;
    private boolean hasCgm = false;
    private boolean hasSuperbWarfare = false;
    private boolean hasSsrV5 = false;

    @Override
    public void onLoad(String mixinPackage) {
        ClassLoader loader = this.getClass().getClassLoader();
        // v5 adds api.event.IEventBus; v4 does not
        hasSsrV5 = loader.getResource("com/github/exopandora/shouldersurfing/api/event/IEventBus.class") != null;
        hasEpicFight = loader.getResource("yesman/epicfight/client/world/capabilites/entitypatch/player/LocalPlayerPatch.class") != null;
        hasTacz = loader.getResource("com/tacz/guns/compat/shouldersurfing/ShoulderSurfingPlugin.class") != null;
        hasRadialAggro = loader.getResource("com/mrbysco/radialaggroindicator/client/HudHandler.class") != null;
        hasValkyrienSkies = loader.getResource("org/valkyrienskies/mod/common/VSGameUtilsKt.class") != null;
        hasFocus = loader.getResource("com/jvn/focus/client/hud/VanillaCrosshairSuppressor.class") != null;
        hasBetterMountSteering = loader.getResource("com/bettermountsteering/handler/MountSteeringHandler.class") != null;
        hasScorchedGuns = loader.getResource("top/ribs/scguns/client/handler/ShootingHandler.class") != null;
        hasCgm = loader.getResource("com/mrcrayfish/guns/client/handler/ShootingHandler.class") != null;
        hasSuperbWarfare = loader.getResource("com/atsuishio/superbwarfare/event/ClientEventHandler.class") != null;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith("V5")) {
            if (!hasSsrV5) return false;
        } else if (mixinClassName.endsWith("V4")) {
            if (hasSsrV5) return false;
        }

        if (mixinClassName.contains("MixinDisableEpicFightSsrLockOnTick") || mixinClassName.contains("MixinForceSsrOffsetDuringLockOn") || mixinClassName.contains("MixinEpicFightForwardRotation") || mixinClassName.contains("MixinEpicFightModelYRotSnap") || mixinClassName.contains("MixinPhantomAscent") || mixinClassName.contains("MixinSyncLockOnCamera") || mixinClassName.contains("MixinSsrDodgeDirection") || mixinClassName.contains("MixinSsrAttackDirection") || mixinClassName.contains("MixinSuppressMovingInputYawFollow")) {
            return hasEpicFight;
        }
        if (mixinClassName.contains("MixinTaczGunHoldDecouple") || mixinClassName.contains("MixinTaczShootFaceCamera")
                || mixinClassName.contains("MixinTaczBulletShipDirection")) {
            return hasTacz;
        }
        if (mixinClassName.contains("MixinRadialAggroCameraYaw")) {
            return hasRadialAggro;
        }
        if (mixinClassName.contains("MixinSyncPlayerToSsrOnVsShip") || mixinClassName.contains("MixinSuppressSsrVsIncompatWarning") || mixinClassName.contains("MixinSsrLevelRendererForVs") || mixinClassName.contains("MixinSkipDynamicOffsetOnVsShip") || mixinClassName.contains("CameraMoveInvoker") || mixinClassName.contains("MixinEntityHelperLookAtOnVsShip")
                || mixinClassName.contains("MixinVsCameraSkipZoomOnSsr") || mixinClassName.contains("MixinSsrSkipMaxZoomOnVsShip")) {
            return hasValkyrienSkies;
        }
        if (mixinClassName.contains("MixinFocusKeepCrosshairForSsr") || mixinClassName.contains("MixinFocusPickOnlyWhenLockedOn")) {
            return hasFocus;
        }
        if (mixinClassName.contains("MixinSsrSuppressFollowDuringMountRotate") || mixinClassName.contains("MixinSsrSuppressTurnSnapback") || mixinClassName.contains("MixinSsrSuppressPassengerConstraint")) {
            return hasBetterMountSteering;
        }
        if (mixinClassName.contains("MixinScorchedGunsFaceCamera")
                || mixinClassName.contains("MixinScorchedFlareFaceCamera")
                || mixinClassName.contains("MixinScorchedProjectileShipDirection")
                || mixinClassName.contains("MixinScorchedArrowShipDirection")
                || mixinClassName.contains("MixinScorchedThrowableShipDirection")
                || mixinClassName.contains("MixinScorchedFlareShipDirection")
                || mixinClassName.contains("MixinScorchedBeamRenderDirection")) {
            return hasScorchedGuns;
        }
        if (mixinClassName.contains("MixinCgmGunFaceCamera") || mixinClassName.contains("MixinCgmGunServerHeadYaw")
                || mixinClassName.contains("MixinCgmMountGunArmFix") || mixinClassName.contains("MixinCgmMountedGunArms")
                || mixinClassName.contains("MixinCgmWeaponPosePitch")
                || mixinClassName.contains("MixinCgmProjectileShipDirection")
                || mixinClassName.contains("MixinCgmKeepCrosshairForSsr")) {
            return hasCgm;
        }
        if (mixinClassName.contains("MixinSuperbWarfareGunFaceCamera")
                || mixinClassName.contains("MixinSwSkipThirdPersonGunCameraOnSsr")
                || mixinClassName.contains("MixinSwSkipThirdPersonCameraYawOnSsr")) {
            return hasSuperbWarfare;
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
