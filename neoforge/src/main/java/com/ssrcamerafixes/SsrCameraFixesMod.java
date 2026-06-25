package com.ssrcamerafixes;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.handler.AimingFaceCameraHandler;
import com.ssrcamerafixes.handler.AttackFaceCameraHandler;
import com.ssrcamerafixes.handler.LockOnCameraSyncHandler;
import com.ssrcamerafixes.handler.ShoulderCycleHandler;
import com.ssrcamerafixes.handler.SprintRotateHandler;
import com.ssrcamerafixes.handler.WalkStopFaceCameraHandler;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Mod(SsrCameraFixesMod.MODID)
public class SsrCameraFixesMod {
    public static final String MODID = "ssrcamerafixes";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String KEY_CATEGORY = Keybinds.CATEGORY;
    public static KeyMapping SHOULDER_CYCLE;

    public SsrCameraFixesMod(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.CLIENT, SsrCameraFixesConfig.CLIENT_CONFIG, "ssrcamerafixes-client.toml");

        modBus.addListener(this::onRegisterKeyMappings);

        NeoForge.EVENT_BUS.register(ShoulderCycleHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(LockOnCameraSyncHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(SprintRotateHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(WalkStopFaceCameraHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(AimingFaceCameraHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(AttackFaceCameraHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(com.ssrcamerafixes.handler.FocusLockOnPerspectiveHandler.INSTANCE);
        NeoForge.EVENT_BUS.register(EpicFightHelper.INSTANCE);

        LOGGER.info("Shoulder Surfing Reloaded: Camera Fixes & Additions v1.0.5 loaded.");
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        SHOULDER_CYCLE = new KeyMapping(
            Keybinds.SHOULDER_CYCLE_NAME,
            GLFW.GLFW_KEY_O,
            KEY_CATEGORY
        );
        event.register(SHOULDER_CYCLE);
        Keybinds.SHOULDER_CYCLE = SHOULDER_CYCLE;
    }
}
