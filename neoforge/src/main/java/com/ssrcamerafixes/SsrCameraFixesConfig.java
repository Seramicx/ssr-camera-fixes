package com.ssrcamerafixes;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SsrCameraFixesConfig {

    public static final ModConfigSpec CLIENT_CONFIG;

    public static final ModConfigSpec.DoubleValue CAMERA_OVERHEAD_OFFSET_Y;
    public static final ModConfigSpec.BooleanValue DISABLE_FOLLOW_PLAYER_ROTATIONS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Overhead Preset").push("camera");

        CAMERA_OVERHEAD_OFFSET_Y = builder
                .comment(
                    "Vertical offset (in blocks) for the OVERHEAD state of the shoulder cycle keybind.",
                    "Shoulder Surfing Reloaded owns the right/left X offset and most other camera knobs;",
                    "this is the only value we still own, since SSR's preset cycling is per-axis and",
                    "can't represent a coupled X=0 + high Y preset directly."
                )
                .defineInRange("cameraOverheadOffsetY", 1.2, -2.0, 4.0);

        DISABLE_FOLLOW_PLAYER_ROTATIONS = builder
                .comment(
                    "If true, suppress SSR's idle camera auto-rotation toward the player's facing direction.",
                    "SSR's followPlayerRotations feature lerps the camera back toward player.yRot after",
                    "the configured idle delay; some users find this disorienting (\"camera moves without",
                    "my consent\"). Setting this to true overrides SSR's behavior at render time without",
                    "modifying SSR's own config file."
                )
                .define("disableFollowPlayerRotations", true);

        builder.pop();

        CLIENT_CONFIG = builder.build();
    }
}
