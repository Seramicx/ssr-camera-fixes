package com.ssrcamerafixes.fabric1211;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.logging.LogUtils;
import com.ssrcamerafixes.Keybinds;
import com.ssrcamerafixes.SsrCameraFixesConfig;
import com.ssrcamerafixes.compat.WizardsHelper;
import com.ssrcamerafixes.fabric1211.handler.AimingFaceCameraHandler;
import com.ssrcamerafixes.fabric1211.handler.AttackFaceCameraHandler;
import com.ssrcamerafixes.fabric1211.handler.ShoulderCycleHandler;
import com.ssrcamerafixes.fabric1211.handler.SprintRotateHandler;
import com.ssrcamerafixes.fabric1211.handler.WalkStopFaceCameraHandler;
import fuzs.forgeconfigapiport.fabric.api.neoforge.v4.NeoForgeConfigRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.neoforged.fml.config.ModConfig;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;

public class SsrCameraFixesFabricMod implements ClientModInitializer {

    public static final String MODID = "ssrcamerafixes";
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitializeClient() {
        NeoForgeConfigRegistry.INSTANCE.register(
                MODID, ModConfig.Type.CLIENT,
                SsrCameraFixesConfig.CLIENT_CONFIG,
                "ssrcamerafixes-client.toml");

        KeyMapping cycle = new KeyMapping(
                Keybinds.SHOULDER_CYCLE_NAME,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                Keybinds.CATEGORY);
        KeyBindingHelper.registerKeyBinding(cycle);
        Keybinds.SHOULDER_CYCLE = cycle;

        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            AttackFaceCameraHandler.INSTANCE.onClientTickStart(client);
            AimingFaceCameraHandler.onClientTickStart(client);
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ShoulderCycleHandler.INSTANCE.onClientTickEnd(client);
            AttackFaceCameraHandler.INSTANCE.onClientTickEnd(client);
            SprintRotateHandler.INSTANCE.onClientTickEnd(client);
            WalkStopFaceCameraHandler.INSTANCE.onClientTickEnd(client);
            WizardsHelper.INSTANCE.tickLatch();
        });

        LOGGER.info("Shoulder Surfing Reloaded: Camera Fixes & Additions v1.0.2 (Fabric 1.21.1) loaded.");
    }
}
