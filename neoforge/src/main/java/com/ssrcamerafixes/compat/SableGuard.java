package com.ssrcamerafixes.compat;

// Set by the mixin plugin, kept free of Shoulder Surfing references so loading it early is harmless
public final class SableGuard {

    public static volatile boolean perspectiveGuard = false;

    private SableGuard() {}
}
