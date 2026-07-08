package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.FreeLookMovementHelper;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;

// Runs after SSR's MovementInputUpdateEvent handler, which returns early during free look.
public final class FreeLookMovementHandler {

    public static final FreeLookMovementHandler INSTANCE = new FreeLookMovementHandler();

    private FreeLookMovementHandler() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onMovementInput(MovementInputUpdateEvent event) {
        FreeLookMovementHelper.applyToInput(event.getInput());
    }
}
