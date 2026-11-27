package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.SableCameraHelper;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public final class SableCameraHandler {

    public static final SableCameraHandler INSTANCE = new SableCameraHandler();

    private boolean sableViewO = false;

    private SableCameraHandler() {}

    // Runs before Shoulder Surfing's own tick so the F5 click never reaches its perspective cycle
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onClientTickPre(ClientTickEvent.Pre event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null || !SableCameraHelper.handlesF5(mc)) return;
        while (mc.options.keyTogglePerspective.consumeClick()) {
            SableCameraHelper.cycle(mc);
        }
    }

    // Sable resets the camera itself on dismount, so remember the view from the previous tick
    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            sableViewO = false;
            return;
        }
        boolean sableView = SableCameraHelper.isSableView(mc.options.getCameraType());
        if ((sableView || sableViewO) && !SableCameraHelper.isRidingSubLevel(mc.getCameraEntity())) {
            SableCameraHelper.leaveSableView(mc);
            sableView = false;
        }
        sableViewO = sableView;
    }
}
