package com.ssrcamerafixes;

import net.minecraftforge.common.ForgeConfigSpec;

import com.ssrcamerafixes.compat.CompatSwitches;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SsrCameraFixesConfig {

    public static final ForgeConfigSpec CLIENT_CONFIG;

    public enum IdleBehavior { VANILLA_3RD_PERSON, DECOUPLED, SSR_DEFAULT }

    public enum TurnMode { FOLLOW_SSR, ALWAYS, NEVER }

    public static final ForgeConfigSpec.DoubleValue CAMERA_OVERHEAD_OFFSET_Y;
    public static final ForgeConfigSpec.BooleanValue DISABLE_FOLLOW_PLAYER_ROTATIONS;
    public static final ForgeConfigSpec.BooleanValue DECOUPLE_HELD_CROSSBOW;
    public static final ForgeConfigSpec.EnumValue<IdleBehavior> IDLE_BEHAVIOR;
    public static final ForgeConfigSpec.EnumValue<TurnMode> TURN_ON_ATTACK;
    public static final ForgeConfigSpec.EnumValue<TurnMode> TURN_ON_USE;
    public static final ForgeConfigSpec.BooleanValue AIM_ALL_ITEMS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> AIM_ITEMS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> AIM_ON_ATTACK_ITEMS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> IGNORED_ITEMS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> AIM_KEYS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> FACE_CAMERA_KEYS;
    public static final ForgeConfigSpec.BooleanValue ATTACK_BODY_SNAP;
    public static final ForgeConfigSpec.BooleanValue AIMING_BODY_FOLLOW;
    public static final ForgeConfigSpec.BooleanValue RESTORE_SHOULDER_SURFING;
    public static final Map<String, ForgeConfigSpec.BooleanValue> COMPAT = new HashMap<>();

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Overhead Preset").push("camera");

        CAMERA_OVERHEAD_OFFSET_Y = builder
                .comment("Vertical offset (blocks) for the OVERHEAD shoulder cycle preset.")
                .defineInRange("cameraOverheadOffsetY", 1.2, -2.0, 4.0);

        DISABLE_FOLLOW_PLAYER_ROTATIONS = builder
                .comment("If true, suppresses SSR's idle camera auto-rotation toward player facing.")
                .define("disableFollowPlayerRotations", true);

        DECOUPLE_HELD_CROSSBOW = builder
                .comment("Keep the camera decoupled while holding a loaded crossbow. Off gives stock Shoulder Surfing behaviour.")
                .define("decoupleHeldCrossbow", false);
        IDLE_BEHAVIOR = builder
                .comment("Idle yaw mode: VANILLA_3RD_PERSON, DECOUPLED, or SSR_DEFAULT.")
                .defineEnum("idleBehavior", IdleBehavior.DECOUPLED);

        builder.pop();

        builder.push("turning");

        TURN_ON_ATTACK = builder
                .comment("Turn your character to the crosshair when you attack. FOLLOW_SSR uses Shoulder Surfing's when_attacking setting.")
                .defineEnum("turnOnAttack", TurnMode.FOLLOW_SSR);

        TURN_ON_USE = builder
                .comment("Turn your character to the crosshair when you use an item, block, cast or shoot. FOLLOW_SSR uses Shoulder Surfing's when_using_item setting.")
                .defineEnum("turnOnUse", TurnMode.FOLLOW_SSR);

        builder.pop();

        builder.comment("Aim at the crosshair for items and keys from any mod.",
                "Entries: item ids (minecraft:bow), every item of a mod (somemod:*), or item tags (#c:tools/bows).",
                "Data packs can also add items to the tags ssrcamerafixes:aim_at_crosshair, ssrcamerafixes:aim_at_crosshair_on_attack and ssrcamerafixes:never_aim.").push("aim");

        AIM_ALL_ITEMS = builder
                .comment("Right-clicking any item that isn't a block or food aims it at the crosshair.")
                .define("aimAllItems", true);

        AIM_ITEMS = builder
                .comment("Items that aim at the crosshair when used, even with aimAllItems off.")
                .defineListAllowEmpty(List.of("aimItems"), () -> List.of(), o -> o instanceof String);

        AIM_ON_ATTACK_ITEMS = builder
                .comment("Items that fire with left click (attack) and should aim at the crosshair.")
                .defineListAllowEmpty(List.of("aimOnAttackItems"), () -> List.of(), o -> o instanceof String);

        IGNORED_ITEMS = builder
                .comment("Items that never get turned to the crosshair by this list system.")
                .defineListAllowEmpty(List.of("ignoredItems"), () -> List.of("tacz:*", "superbwarfare:*"), o -> o instanceof String);

        AIM_KEYS = builder
                .comment("Keybinds that cast or fire something (spell keys, ability keys). Use the key's name from options.txt without the key_ prefix, e.g. key.irons_spellbooks.spell_wheel. A trailing * matches every key that starts with the text before it.")
                .defineListAllowEmpty(List.of("aimKeys"), () -> List.of("key.ars_nouveau.qc*", "key.cataclysm.ability", "key.cataclysm.helmet_ability"), o -> o instanceof String);

        FACE_CAMERA_KEYS = builder
                .comment("Dash, roll or dodge keybinds. Pressing one turns your character to face the camera direction first, so the move goes where you are looking. Same name format as aimKeys.")
                .defineListAllowEmpty(List.of("faceCameraKeys"), () -> List.of(), o -> o instanceof String);

        builder.pop();

        builder.push("behavior");

        ATTACK_BODY_SNAP = builder
                .comment("Your body and head follow the swing when you attack.")
                .define("attackBodySnap", true);

        AIMING_BODY_FOLLOW = builder
                .comment("Your body follows the crosshair while drawing a bow, casting, eating or blocking.")
                .define("aimingBodyFollow", true);

        RESTORE_SHOULDER_SURFING = builder
                .comment("Switch back to shoulder surfing when another mod drops you into plain third person without you pressing a perspective key. Leave off if a mod uses plain third person on purpose for its own camera.")
                .define("restoreShoulderSurfing", false);

        builder.pop();

        builder.comment("Turn off this mod's support for a single mod. Restart the game after changing these.").push("compat");
        for (String key : CompatSwitches.keys()) {
            COMPAT.put(key, builder.define(key, true));
        }

        builder.pop();

        CLIENT_CONFIG = builder.build();
    }
}
