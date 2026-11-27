package com.ssrcamerafixes.handler;

import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.compat.SmallShipsHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.bus.api.SubscribeEvent;

// Small Ships' free look skips the boat clamp that faces the rider's body to the bow, so in third person the body spins with the camera
public final class SmallShipsBodyHandler {

    public static final SmallShipsBodyHandler INSTANCE = new SmallShipsBodyHandler();

    private SmallShipsBodyHandler() {}

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        Entity vehicle = player.getVehicle();
        if (!SmallShipsHelper.isShip(vehicle) || !ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        faceBow(player, vehicle);
    }

    @SubscribeEvent
    public void onRenderFrame(RenderFrameEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        Entity vehicle = player.getVehicle();
        if (!SmallShipsHelper.isShip(vehicle) || !ShoulderSurfingHelper.isShoulderSurfingActive()) return;
        faceBow(player, vehicle);
    }

    // set both ends of the render lerp, other client code still pulls the body toward the head between ticks
    private static void faceBow(LocalPlayer player, Entity vehicle) {
        player.yBodyRotO = vehicle.yRotO;
        player.yBodyRot = vehicle.getYRot();
    }
}
