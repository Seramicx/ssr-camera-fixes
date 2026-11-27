package com.ssrcamerafixes.fabric1211.handler;

import com.ssrcamerafixes.compat.AimRules;
import com.ssrcamerafixes.compat.BetterMountSteeringHelper;
import com.ssrcamerafixes.compat.ShoulderSurfingHelper;
import com.ssrcamerafixes.fabric1211.mixin.AccessorKeyMappingClicks;
import net.minecraft.client.CameraType;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.entity.Mob;
import com.ssrcamerafixes.compat.JegHelper;
import java.util.HashSet;
import java.util.Set;

public class UniversalAimHandler {

    public static final UniversalAimHandler INSTANCE = new UniversalAimHandler();

    private int keyLatch;
    private int faceLatch;
    private Set<KeyMapping> faceKeysDownO = Set.of();
    private int flushedTick = -1;
    private int tick;
    private boolean sending;

    private UniversalAimHandler() {}

    public void onClientTickStart(Minecraft mc) {
        tick++;
        JegHelper.flushDeferredShot();
        if (keyLatch > 0) keyLatch--;
        if (faceLatch > 0) faceLatch--;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !canAim(player)) return;
        if (faceKeyPressed()) {
            faceLatch = 6;
            faceCamera(player);
        } else if (aimKeyActive()) {
            keyLatch = 6;
            if (AimRules.useTurnAllowed()) aim(player);
        } else if (player.isUsingItem() && AimRules.aimsOnUse(player.getUseItem()) && AimRules.useTurnAllowed()) {
            ShoulderSurfingHelper.lookAtCrosshairTarget();
        }
    }

    public void beforeUse() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !canAim(player) || !AimRules.useTurnAllowed()) return;
        if (AimRules.aimsOnUse(player.getMainHandItem()) || AimRules.aimsOnUse(player.getOffhandItem())) {
            ShoulderSurfingHelper.lookAtCrosshairTarget();
        }
    }

    public void beforeAttack() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !canAim(player) || !AimRules.useTurnAllowed()) return;
        if (AimRules.aimsOnAttack(player.getMainHandItem())) {
            aim(player);
        }
    }

    public void beforeRelease() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !canAim(player) || !AimRules.useTurnAllowed()) return;
        if (AimRules.aimsOnUse(player.getUseItem())) {
            aim(player);
        }
    }

    // mods that send their cast or fire packet from a key callback skip the tick hook entirely
    public void beforeModPacket() {
        if (sending || flushedTick == tick || !Minecraft.getInstance().isSameThread()) return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !canAim(player)) return;
        if (faceLatch > 0) {
            faceCamera(player);
            return;
        }
        boolean intent = keyLatch > 0 || aimKeyActive()
                || Minecraft.getInstance().options.keyUse.isDown() && AimRules.aimsOnUse(player.getMainHandItem())
                || Minecraft.getInstance().options.keyAttack.isDown() && AimRules.aimsOnAttack(player.getMainHandItem());
        if (intent && AimRules.useTurnAllowed()) aim(player);
    }

    private void aim(LocalPlayer player) {
        ShoulderSurfingHelper.lookAtCrosshairTarget();
        sendRotation(player);
    }

    private void faceCamera(LocalPlayer player) {
        float yaw = ShoulderSurfingHelper.getCameraYaw();
        player.setYRot(yaw);
        player.yRotO = yaw;
        player.yBodyRot = yaw;
        player.yHeadRot = yaw;
        sendRotation(player);
    }

    private void sendRotation(LocalPlayer player) {
        ClientPacketListener conn = Minecraft.getInstance().getConnection();
        if (conn == null) return;
        sending = true;
        try {
            conn.send(new ServerboundMovePlayerPacket.Rot(player.getYRot(), player.getXRot(), player.onGround()));
        } finally {
            sending = false;
        }
        flushedTick = tick;
    }

    // some mods consume the click in their own tick before this runs, so also catch the press edge
    private boolean faceKeyPressed() {
        boolean pressed = false;
        Set<KeyMapping> down = new HashSet<>();
        for (KeyMapping k : AimRules.faceCameraKeys()) {
            if (k.isDown()) down.add(k);
            if (((AccessorKeyMappingClicks) k).ssrcamerafixes$getClickCount() > 0 || k.isDown() && !faceKeysDownO.contains(k)) pressed = true;
        }
        faceKeysDownO = down;
        return pressed;
    }

    private boolean aimKeyActive() {
        for (KeyMapping k : AimRules.aimKeys()) {
            if (k.isDown() || ((AccessorKeyMappingClicks) k).ssrcamerafixes$getClickCount() > 0) return true;
        }
        return false;
    }

    private static boolean canAim(LocalPlayer player) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null || mc.getCameraEntity() != player) return false;
        if (mc.options.getCameraType() != CameraType.THIRD_PERSON_BACK) return false;
        if (!ShoulderSurfingHelper.isCameraDecoupled() || AimRules.isFreeLooking()) return false;
        if (player.isFallFlying() || player.isSleeping()) return false;
        if (player.getVehicle() instanceof Mob mob && mob.getControllingPassenger() == player) return false;
        return !BetterMountSteeringHelper.isMountRotateActive() && !BetterMountSteeringHelper.isDecoupleTransitioning();
    }
}
