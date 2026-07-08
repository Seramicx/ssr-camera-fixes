package com.ssrcamerafixes;

import com.mojang.logging.LogUtils;
import com.ssrcamerafixes.compat.EpicFightHelper;
import com.ssrcamerafixes.handler.AimingFaceCameraHandler;
import com.ssrcamerafixes.handler.AttackFaceCameraHandler;
import com.ssrcamerafixes.handler.LockOnCameraSyncHandler;
import com.ssrcamerafixes.handler.FreeLookMovementHandler;
import com.ssrcamerafixes.handler.ShoulderCycleHandler;
import com.ssrcamerafixes.handler.SprintRotateHandler;
import com.ssrcamerafixes.handler.WalkStopFaceCameraHandler;
import com.ssrcamerafixes.handler.WallClimbBodyLockHandler;
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

    public static final String KEY_CATEGORY = Keybinds.CATEGORY;
    public static KeyMapping SHOULDER_CYCLE;

    public SsrCameraFixesMod(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.CLIENT, SsrCameraFixesConfig.CLIENT_CONFIG, "ssrcamerafixes-client.toml");

        context.getModEventBus().addListener(this::onRegisterKeyMappings);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(ShoulderCycleHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(WallClimbBodyLockHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(LockOnCameraSyncHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(SprintRotateHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(WalkStopFaceCameraHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(FreeLookMovementHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(AimingFaceCameraHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(AttackFaceCameraHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(com.ssrcamerafixes.handler.WomAquaCameraFrameHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(com.ssrcamerafixes.handler.FocusLockOnPerspectiveHandler.INSTANCE);
        MinecraftForge.EVENT_BUS.register(EpicFightHelper.INSTANCE);

        com.ssrcamerafixes.compat.BetterMountSteeringHelper.registerCameraSource();

        LOGGER.info("Shoulder Surfing Reloaded: Camera Fixes & Additions v2.0.0 loaded.");
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
