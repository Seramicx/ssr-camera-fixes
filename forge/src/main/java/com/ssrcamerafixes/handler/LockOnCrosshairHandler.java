package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.SsrCameraFixesMod;
import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.compat.SuperbWarfareHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SsrCameraFixesMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class LockOnCrosshairHandler {

    private LockOnCrosshairHandler() {}

    // HIGHEST so vanilla crosshair is hidden before other crosshair-rendering mods process the event.
    // SW: only while ADS (zoom). holdingFireKey alone must NOT hide — dry-fire during reload sets that
    // flag with no shot and no SW replacement reticle, so the SSR/vanilla crosshair would just vanish.
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPreCrosshair(RenderGuiOverlayEvent.Pre event) {
        if (!VanillaGuiOverlay.CROSSHAIR.id().equals(event.getOverlay().id())) return;
        if (!EpicFightHelper.isLockOnTargeting() && !SuperbWarfareHelper.isZooming()) return;
        event.setCanceled(true);
    }
}
