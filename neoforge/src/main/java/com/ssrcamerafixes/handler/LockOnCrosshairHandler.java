package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.SsrCameraFixesMod;
import com.ssrcamerafixes.compat.EpicFightHelper;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

@EventBusSubscriber(modid = SsrCameraFixesMod.MODID, value = Dist.CLIENT)
public final class LockOnCrosshairHandler {

    private LockOnCrosshairHandler() {}

    // HIGHEST so vanilla crosshair is hidden before other crosshair-rendering mods process the event
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPreCrosshair(RenderGuiLayerEvent.Pre event) {
        if (!VanillaGuiLayers.CROSSHAIR.equals(event.getName())) return;
        if (!EpicFightHelper.isLockOnTargeting()) return;
        event.setCanceled(true);
    }
}
