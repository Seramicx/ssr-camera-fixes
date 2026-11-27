package com.ssrcamerafixes.mixin;

import net.minecraft.client.CameraType;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Options.class)
public interface AccessorOptionsCameraType {

    @Accessor("cameraType")
    void ssrcamerafixes$setCameraTypeField(CameraType cameraType);
}
