package com.ssrcamerafixes.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.ssrcamerafixes.command.ConfigCommands;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyMapping.class)
public class MixinKeyCaptureClick {
    @Inject(method = "click", at = @At("HEAD"))
    private static void ssrcamerafixes$capture(InputConstants.Key key, CallbackInfo ci) {
        ConfigCommands.INSTANCE.onKeyClick(key);
    }
}
