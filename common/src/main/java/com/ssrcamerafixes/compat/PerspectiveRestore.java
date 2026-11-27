package com.ssrcamerafixes.compat;

import com.ssrcamerafixes.SsrCameraFixesConfig;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.function.Predicate;

public final class PerspectiveRestore {

    public static final PerspectiveRestore INSTANCE = new PerspectiveRestore();

    private boolean ssrO;
    private boolean playerChose;
    private boolean lostToMod;

    private PerspectiveRestore() {}

    public void tickStart(Minecraft mc, Predicate<KeyMapping> clicked) {
        playerChose = false;
        for (KeyMapping k : mc.options.keyMappings) {
            if ((k == mc.options.keyTogglePerspective || "Shoulder Surfing".equals(k.getCategory())) && (k.isDown() || clicked.test(k))) {
                playerChose = true;
                return;
            }
        }
    }

    public void tickEnd(Minecraft mc) {
        boolean ssr = ShoulderSurfingHelper.isShoulderSurfingPerspective();
        boolean ours = ShoulderSurfingHelper.switchedAwayByUs;
        ShoulderSurfingHelper.switchedAwayByUs = false;
        if (mc.player == null || !AimRules.get(SsrCameraFixesConfig.RESTORE_SHOULDER_SURFING, false)) {
            lostToMod = false;
            ssrO = ssr;
            return;
        }
        if (ssr || playerChose || ours) {
            lostToMod = false;
        } else if (ssrO) {
            lostToMod = true;
        }
        if (lostToMod && mc.screen == null && ShoulderSurfingHelper.isThirdPersonBackPerspective()) {
            ShoulderSurfingHelper.changeToShoulderSurfing();
            lostToMod = false;
            ssr = true;
        }
        ssrO = ssr;
    }
}
