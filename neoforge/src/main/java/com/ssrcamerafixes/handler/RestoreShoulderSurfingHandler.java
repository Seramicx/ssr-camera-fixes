package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.PerspectiveRestore;
import com.ssrcamerafixes.mixin.AccessorKeyMappingClicks;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public class RestoreShoulderSurfingHandler {

    public static final RestoreShoulderSurfingHandler INSTANCE = new RestoreShoulderSurfingHandler();

    private RestoreShoulderSurfingHandler() {}

    @SubscribeEvent
    public void onClientTickPre(ClientTickEvent.Pre event) {
        PerspectiveRestore.INSTANCE.tickStart(Minecraft.getInstance(), k -> ((AccessorKeyMappingClicks) k).ssrcamerafixes$getClickCount() > 0);
    }

    @SubscribeEvent
    public void onClientTickPost(ClientTickEvent.Post event) {
        PerspectiveRestore.INSTANCE.tickEnd(Minecraft.getInstance());
    }
}
