package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

// Backward roll looks reversed once the body turns to the away direction, so swap it for the forward clip.
// Target LocalPlayerPatch: its playAnimationInClientSide override never calls super, so a base-class mixin won't run.
//
// EF sets modelYRot for the BACKWARD clip (face camera / +strafe). FORWARD needs face-into-move instead.
// ModelYRotSnap cannot fix this mid-roll: ActionAnimation sets turningLocked for the whole clip.
@Pseudo
@Mixin(targets = "yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch", remap = false)
public abstract class MixinPhantomAscentForwardRoll {

    @ModifyVariable(method = "playAnimationInClientSide", at = @At("HEAD"), argsOnly = true, require = 0, remap = false)
    private AssetAccessor<?> ssrcamerafixes$forwardRoll(AssetAccessor<?> animation) {
        if (!ShoulderSurfingHelper.isCameraDecoupled()) return animation;
        boolean backward = animation == Animations.BIPED_PHANTOM_ASCENT_BACKWARD;
        if (backward || animation == Animations.BIPED_PHANTOM_ASCENT_FORWARD) {
            Float facing = ssrcamerafixes$moverFacingFromInput();
            if (facing != null) {
                ((PlayerPatch<?>) (Object) this).setModelYRot(facing, true);
            }
            return backward ? Animations.BIPED_PHANTOM_ASCENT_FORWARD : animation;
        }
        return animation;
    }

    private static Float ssrcamerafixes$moverFacingFromInput() {
        Minecraft mc = Minecraft.getInstance();
        Options options = mc.options;
        LocalPlayer player = mc.player;
        if (options == null || player == null) return null;
        int forward = (options.keyUp.isDown() ? 1 : 0) + (options.keyDown.isDown() ? -1 : 0);
        int strafeLeft = (options.keyLeft.isDown() ? 1 : 0) + (options.keyRight.isDown() ? -1 : 0);
        if (forward == 0 && strafeLeft == 0) return null;
        float offset = (float) -Math.toDegrees(Math.atan2(strafeLeft, forward));
        return Mth.wrapDegrees(ShoulderSurfingHelper.getCameraYaw() + offset);
    }
}
