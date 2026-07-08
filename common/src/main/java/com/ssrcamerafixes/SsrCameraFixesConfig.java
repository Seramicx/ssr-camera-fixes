package com.ssrcamerafixes;

import net.minecraftforge.common.ForgeConfigSpec;

public class SsrCameraFixesConfig {

    public static final ForgeConfigSpec CLIENT_CONFIG;

    public enum IdleBehavior { VANILLA_3RD_PERSON, DECOUPLED, SSR_DEFAULT }

    public static final ForgeConfigSpec.DoubleValue CAMERA_OVERHEAD_OFFSET_Y;
    public static final ForgeConfigSpec.BooleanValue DISABLE_FOLLOW_PLAYER_ROTATIONS;
    public static final ForgeConfigSpec.EnumValue<IdleBehavior> IDLE_BEHAVIOR;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Overhead Preset").push("camera");

        CAMERA_OVERHEAD_OFFSET_Y = builder
                .comment("Vertical offset (blocks) for the OVERHEAD shoulder cycle preset.")
                .defineInRange("cameraOverheadOffsetY", 1.2, -2.0, 4.0);

        DISABLE_FOLLOW_PLAYER_ROTATIONS = builder
                .comment("If true, suppresses SSR's idle camera auto-rotation toward player facing.")
                .define("disableFollowPlayerRotations", true);

        IDLE_BEHAVIOR = builder
                .comment("Idle yaw mode: VANILLA_3RD_PERSON, DECOUPLED, or SSR_DEFAULT.")
                .defineEnum("idleBehavior", IdleBehavior.DECOUPLED);

        builder.pop();

        CLIENT_CONFIG = builder.build();
    }
}
