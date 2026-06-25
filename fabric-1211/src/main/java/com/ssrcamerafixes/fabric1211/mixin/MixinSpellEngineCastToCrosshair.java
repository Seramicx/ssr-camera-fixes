package com.ssrcamerafixes.fabric1211.mixin;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Spell Engine resolves every spell's target from the caster's body look vector (TargetHelper /
// SpellHelper read getRotationVec). Instant casts like Shadowstep resolve in the same tick the cast
// starts, before any tick handler can turn the body, so they fire where the body faces, not the
// crosshair. Snapping the player to the SSR camera here aims the spell and turns the character at once.
@Pseudo
@Mixin(LocalPlayer.class)
public abstract class MixinSpellEngineCastToCrosshair {

    @Inject(method = "startSpellCast", at = @At("HEAD"), require = 0, remap = false)
    private void ssrcamerafixes$aimSpellAtCrosshair(CallbackInfoReturnable<Object> cir) {
        if (!ShoulderSurfingHelper.isCameraDecoupled()) return;
        LocalPlayer player = (LocalPlayer) (Object) this;
        Entity vehicle = player.getVehicle();
        if (vehicle instanceof Mob mob && mob.getControllingPassenger() == player) return;

        float camYaw = Mth.wrapDegrees(ShoulderSurfingHelper.getCameraYaw());
        float camXRot = ShoulderSurfingHelper.getCameraXRot();
        player.setYRot(camYaw);
        player.yRotO = camYaw;
        player.setXRot(camXRot);
        player.xRotO = camXRot;
        player.yBodyRot = camYaw;
        player.yBodyRotO = camYaw;
        player.yHeadRot = camYaw;
        player.yHeadRotO = camYaw;

        ClientPacketListener conn = Minecraft.getInstance().getConnection();
        if (conn != null) {
            conn.send(new ServerboundMovePlayerPacket.Rot(camYaw, camXRot, player.onGround()));
        }
    }
}
