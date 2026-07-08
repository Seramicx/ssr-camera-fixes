package com.ssrcamerafixes.mixin;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraMoveInvoker {
    @Invoker("move")
    void ssrcamerafixes$callMove(double distanceOffset, double verticalOffset, double lateralOffset);
}
