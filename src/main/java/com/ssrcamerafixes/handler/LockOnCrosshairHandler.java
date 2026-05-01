package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.SsrCameraFixesMod;
import com.ssrcamerafixes.compat.EpicFightHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Cancels the vanilla crosshair while EpicFight lock-on is active. SSR draws
 * its own adaptive crosshair, so without this you'd see both at once whenever
 * you lock on.
 */
@Mod.EventBusSubscriber(modid = SsrCameraFixesMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class LockOnCrosshairHandler {

    private LockOnCrosshairHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPreCrosshair(RenderGuiOverlayEvent.Pre event) {
        if (!VanillaGuiOverlay.CROSSHAIR.id().equals(event.getOverlay().id())) return;
        if (!EpicFightHelper.isLockOnTargeting()) return;
        event.setCanceled(true);
    }
}
