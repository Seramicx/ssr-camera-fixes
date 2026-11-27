package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.PerspectiveRestore;
import com.ssrcamerafixes.mixin.AccessorKeyMappingClicks;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RestoreShoulderSurfingHandler {

    public static final RestoreShoulderSurfingHandler INSTANCE = new RestoreShoulderSurfingHandler();

    private RestoreShoulderSurfingHandler() {}

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            PerspectiveRestore.INSTANCE.tickStart(Minecraft.getInstance(), k -> ((AccessorKeyMappingClicks) k).ssrcamerafixes$getClickCount() > 0);
        } else {
            PerspectiveRestore.INSTANCE.tickEnd(Minecraft.getInstance());
        }
    }
}
