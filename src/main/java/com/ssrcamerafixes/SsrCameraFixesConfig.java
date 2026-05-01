package com.ssrcamerafixes;

import net.minecraftforge.common.ForgeConfigSpec;

public class SsrCameraFixesConfig {

    public static final ForgeConfigSpec CLIENT_CONFIG;

    public static final ForgeConfigSpec.DoubleValue CAMERA_OVERHEAD_OFFSET_Y;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Overhead Preset").push("camera");

        CAMERA_OVERHEAD_OFFSET_Y = builder
                .comment(
                    "Vertical offset (in blocks) for the OVERHEAD state of the shoulder cycle keybind.",
                    "Shoulder Surfing Reloaded owns the right/left X offset and most other camera knobs;",
                    "this is the only value we still own, since SSR's preset cycling is per-axis and",
                    "can't represent a coupled X=0 + high Y preset directly."
                )
                .defineInRange("cameraOverheadOffsetY", 1.2, -2.0, 4.0);

        builder.pop();

        CLIENT_CONFIG = builder.build();
    }
}
