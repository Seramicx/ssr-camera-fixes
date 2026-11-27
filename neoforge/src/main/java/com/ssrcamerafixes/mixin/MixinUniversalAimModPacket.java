package com.ssrcamerafixes.mixin;

import com.ssrcamerafixes.handler.UniversalAimHandler;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Connection.class)
public abstract class MixinUniversalAimModPacket {

    @Inject(method = "send(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketSendListener;Z)V", at = @At("HEAD"))
    private void ssrcamerafixes$aimBeforeModPacket(Packet<?> packet, @Nullable PacketSendListener listener, boolean flush, CallbackInfo ci) {
        if (packet instanceof ServerboundCustomPayloadPacket) {
            UniversalAimHandler.INSTANCE.beforeModPacket();
        }
    }
}
