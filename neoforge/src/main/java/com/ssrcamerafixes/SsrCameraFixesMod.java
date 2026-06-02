package com.ssrcamerafixes;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
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
