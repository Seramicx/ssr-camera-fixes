package com.ssrcamerafixes.compat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Mixins are picked before the config system loads, so the [compat] section is read straight from the file
public final class CompatSwitches {

    private static final String[] KEYS = {"epicfight", "bettercombat", "focus", "irons_spellbooks", "irons_artifice", "tacz", "superbwarfare", "scguns", "cgm", "jeg", "musketmod", "create", "radialaggroindicator", "punchymoves", "cinematic", "smallships", "sableplayerragdoll", "sable", "immersive_aircraft", "combatroll", "bettermountsteering", "confluence"};

    private static Map<String, Boolean> values;

    private CompatSwitches() {}

    public static String[] keys() {
        return KEYS;
    }

    public static boolean enabled(String key) {
        if (values == null) values = read();
        return values.getOrDefault(key, true);
    }

    public static List<String> keysForMixin(String mixinClassName) {
        String n = mixinClassName.substring(mixinClassName.lastIndexOf('.') + 1);
        List<String> keys = new ArrayList<>();
        if (n.contains("EpicFight") || n.contains("PhantomAscent") || n.contains("SyncLockOnCamera") || n.contains("SsrDodgeDirection")
                || n.contains("SsrAttackDirection") || n.contains("ForceSsrOffsetDuringLockOn") || n.contains("SuppressMovingInputYawFollow")) keys.add("epicfight");
        if (n.contains("Tacz")) keys.add("tacz");
        if (n.contains("RadialAggro")) keys.add("radialaggroindicator");
        if (n.contains("ShipDirection") || n.contains("VsShip") || n.contains("ForVs") || n.contains("VsCamera") || n.contains("CameraMoveInvoker")
                || n.contains("SsrVsIncompat")) keys.add("valkyrienskies");
        if (n.contains("Focus")) keys.add("focus");
        if (n.contains("SuppressFollowDuringMountRotate") || n.contains("SsrSuppressTurnSnapback") || n.contains("SsrSuppressPassengerConstraint")) keys.add("bettermountsteering");
        if (n.contains("Scorched")) keys.add("scguns");
        if (n.contains("Cgm")) keys.add("cgm");
        if (n.contains("SuperbWarfare") || n.startsWith("MixinSw")) keys.add("superbwarfare");
        if (n.contains("Jeg")) keys.add("jeg");
        if (n.contains("Musket")) keys.add("musketmod");
        if (n.contains("PotatoCannon")) keys.add("create");
        if (n.contains("Irons")) keys.add("irons_spellbooks");
        if (n.contains("Artifice")) keys.add("irons_artifice");
        if (n.contains("Wizardry")) keys.add("ebwizardry");
        if (n.contains("Confluence")) keys.add("confluence");
        if (n.contains("SpellEngine")) keys.add("spell_engine");
        if (n.contains("CombatRoll")) keys.add("combatroll");
        if (n.contains("IaCamera")) keys.add("immersive_aircraft");
        if (n.contains("SableRagdoll")) keys.add("sableplayerragdoll");
        if (n.contains("SableCamera")) keys.add("sable");
        if (n.startsWith("MixinWm")) keys.add("weaponmaster");
        return keys;
    }

    public static boolean mixinEnabled(String mixinClassName) {
        for (String key : keysForMixin(mixinClassName)) {
            if (!enabled(key)) return false;
        }
        return true;
    }

    private static Map<String, Boolean> read() {
        Map<String, Boolean> map = new HashMap<>();
        Path dir = configDir();
        if (dir == null) return map;
        Path file = dir.resolve("ssrcamerafixes-client.toml");
        if (!Files.isRegularFile(file)) return map;
        try {
            boolean inCompat = false;
            for (String line : Files.readAllLines(file)) {
                String s = line.trim();
                if (s.startsWith("[")) {
                    inCompat = s.equals("[compat]");
                    continue;
                }
                if (!inCompat || s.startsWith("#")) continue;
                int eq = s.indexOf('=');
                if (eq < 0) continue;
                map.put(s.substring(0, eq).trim(), !s.substring(eq + 1).trim().startsWith("false"));
            }
        } catch (IOException ignored) {
        }
        return map;
    }

    private static Path configDir() {
        for (String paths : new String[]{"net.minecraftforge.fml.loading.FMLPaths", "net.neoforged.fml.loading.FMLPaths"}) {
            try {
                Object configDir = Class.forName(paths).getField("CONFIGDIR").get(null);
                return (Path) configDir.getClass().getMethod("get").invoke(configDir);
            } catch (Throwable ignored) {
            }
        }
        try {
            Class<?> fabric = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object instance = fabric.getMethod("getInstance").invoke(null);
            return (Path) fabric.getMethod("getConfigDir").invoke(instance);
        } catch (Throwable ignored) {
        }
        return null;
    }
}
