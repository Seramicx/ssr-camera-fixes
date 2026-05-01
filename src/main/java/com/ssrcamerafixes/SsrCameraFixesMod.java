package com.ssrcamerafixes;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

@Mod(SsrCameraFixesMod.MODID)
public class SsrCameraFixesMod {
    public static final String MODID = "ssrcamerafixes";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String KEY_CATEGORY = "key.categories.ssrcamerafixes";
    public static KeyMapping SHOULDER_CYCLE;

    public SsrCameraFixesMod(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.CLIENT, SsrCameraFixesConfig.CLIENT_CONFIG, "ssrcamerafixes-client.toml");

        context.getModEventBus().addListener(this::onRegisterKeyMappings);

        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Shoulder Surfing Reloaded: Camera Fixes & Additions v1.0.0 loaded.");
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        SHOULDER_CYCLE = new KeyMapping(
            "key.ssrcamerafixes.shoulder_cycle",
            GLFW.GLFW_KEY_O,
            KEY_CATEGORY
        );
        event.register(SHOULDER_CYCLE);
    }
}
