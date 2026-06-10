package com.ssrcamerafixes;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SsrCameraFixesConfig {

    public enum IdleBehavior { VANILLA_3RD_PERSON, DECOUPLED, SSR_DEFAULT }

    public static final ModConfigSpec CLIENT_CONFIG;

    public static final ModConfigSpec.DoubleValue CAMERA_OVERHEAD_OFFSET_Y;
    public static final ModConfigSpec.BooleanValue DISABLE_FOLLOW_PLAYER_ROTATIONS;
    public static final ModConfigSpec.EnumValue<IdleBehavior> IDLE_BEHAVIOR;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

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
