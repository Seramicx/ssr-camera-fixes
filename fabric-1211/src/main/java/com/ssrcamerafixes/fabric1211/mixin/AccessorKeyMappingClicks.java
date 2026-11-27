package com.ssrcamerafixes.fabric1211.mixin;

import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(KeyMapping.class)
public interface AccessorKeyMappingClicks {
    @Accessor("clickCount")
    int ssrcamerafixes$getClickCount();
}
