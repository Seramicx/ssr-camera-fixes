package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.FreeLookMovementHelper;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// Runs after SSR's MovementInputUpdateEvent handler, which returns early during free look.
public final class FreeLookMovementHandler {

    public static final FreeLookMovementHandler INSTANCE = new FreeLookMovementHandler();

    private FreeLookMovementHandler() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMovementInput(MovementInputUpdateEvent event) {
        FreeLookMovementHelper.applyToInput(event.getInput());
    }
}
