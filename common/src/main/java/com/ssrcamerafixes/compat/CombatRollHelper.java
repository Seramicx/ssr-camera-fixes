package com.ssrcamerafixes.compat;

public final class CombatRollHelper {

    private static volatile boolean resolved;
    private static volatile boolean loaded;

    private CombatRollHelper() {}

    private static synchronized void resolve() {
        if (resolved) return;
        resolved = true;
        try {
            Class.forName("net.combatroll.CombatRoll");
            loaded = true;
        } catch (ClassNotFoundException ignored) {}
    }

    public static boolean isLoaded() {
        if (!resolved) resolve();
        return loaded;
    }
}
