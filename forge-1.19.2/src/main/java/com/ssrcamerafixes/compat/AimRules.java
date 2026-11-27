package com.ssrcamerafixes.compat;

import com.github.exopandora.shouldersurfing.api.model.TurningMode;
import com.ssrcamerafixes.SsrCameraFixesConfig;
import com.ssrcamerafixes.SsrCameraFixesConfig.TurnMode;
import net.minecraft.client.Camera;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;

public final class AimRules {

    public static final TagKey<Item> AIM_TAG = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("ssrcamerafixes", "aim_at_crosshair"));
    public static final TagKey<Item> AIM_ON_ATTACK_TAG = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("ssrcamerafixes", "aim_at_crosshair_on_attack"));
    public static final TagKey<Item> NEVER_TAG = TagKey.create(Registry.ITEM_REGISTRY, new ResourceLocation("ssrcamerafixes", "never_aim"));

    private static final KeyList AIM_KEYS = new KeyList();
    private static final KeyList FACE_CAMERA_KEYS = new KeyList();

    private AimRules() {}

    public static boolean attackTurnAllowed() {
        TurnMode mode = get(SsrCameraFixesConfig.TURN_ON_ATTACK, TurnMode.FOLLOW_SSR);
        if (mode != TurnMode.FOLLOW_SSR) return mode == TurnMode.ALWAYS;
        TurningMode ssr = ssrAttackMode();
        if (ssr == TurningMode.REQUIRES_TARGET) return hasTargetNear(6.0);
        return ssr != TurningMode.NEVER;
    }

    public static boolean useTurnAllowed() {
        TurnMode mode = get(SsrCameraFixesConfig.TURN_ON_USE, TurnMode.FOLLOW_SSR);
        if (mode != TurnMode.FOLLOW_SSR) return mode == TurnMode.ALWAYS;
        TurningMode ssr = ssrUseMode();
        if (ssr == TurningMode.REQUIRES_TARGET) {
            HitResult hit = Minecraft.getInstance().hitResult;
            return hit != null && hit.getType() != HitResult.Type.MISS;
        }
        return ssr != TurningMode.NEVER;
    }

    public static void lookAtCrosshairForUse() {
        if (!isFreeLooking() && useTurnAllowed()) ShoulderSurfingHelper.lookAtCrosshairTarget();
    }

    public static boolean isFreeLooking() {
        try {
            return ShoulderSurfingHelper.instanceOrNull().isFreeLooking();
        } catch (Throwable t) {
            return false;
        }
    }

    private static TurningMode ssrAttackMode() {
        try {
            return ShoulderSurfingHelper.instanceOrNull().getClientConfig().getTurningModeWhenAttacking();
        } catch (Throwable t) {
            return TurningMode.ALWAYS;
        }
    }

    private static TurningMode ssrUseMode() {
        try {
            return ShoulderSurfingHelper.instanceOrNull().getClientConfig().getTurningModeWhenUsingItem();
        } catch (Throwable t) {
            return TurningMode.ALWAYS;
        }
    }

    // SSR's own pick only reaches vanilla attack range, so Better Combat spears and other long weapons
    // would never count as having a target
    private static boolean hasTargetNear(double range) {
        Minecraft mc = Minecraft.getInstance();
        HitResult hit = mc.hitResult;
        if (hit != null && hit.getType() != HitResult.Type.MISS) return true;
        if (mc.player == null || mc.level == null) return false;
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 from = camera.getPosition();
        double reach = range + from.distanceTo(mc.player.getEyePosition());
        Vec3 to = from.add(new Vec3(camera.getLookVector().x(), camera.getLookVector().y(), camera.getLookVector().z()).scale(reach));
        AABB box = new AABB(from, to).inflate(1.0);
        return ProjectileUtil.getEntityHitResult(mc.player, from, to, box,
                e -> e != mc.player && e.isPickable() && !e.isSpectator(), reach * reach) != null;
    }

    public static boolean aimsOnUse(ItemStack stack) {
        if (stack.isEmpty() || isIgnored(stack)) return false;
        if (stack.is(AIM_TAG) || matches(stack, get(SsrCameraFixesConfig.AIM_ITEMS, List.of()))) return true;
        if (!get(SsrCameraFixesConfig.AIM_ALL_ITEMS, true)) return false;
        if (stack.getItem() instanceof BlockItem || stack.isEdible()) return false;
        UseAnim anim = stack.getUseAnimation();
        return anim != UseAnim.EAT && anim != UseAnim.DRINK;
    }

    public static boolean aimsOnAttack(ItemStack stack) {
        if (stack.isEmpty() || isIgnored(stack)) return false;
        return stack.is(AIM_ON_ATTACK_TAG) || matches(stack, get(SsrCameraFixesConfig.AIM_ON_ATTACK_ITEMS, List.of()));
    }

    private static boolean isIgnored(ItemStack stack) {
        return stack.is(NEVER_TAG) || matches(stack, get(SsrCameraFixesConfig.IGNORED_ITEMS, List.of()));
    }

    private static boolean matches(ItemStack stack, List<? extends String> entries) {
        if (entries.isEmpty()) return false;
        ResourceLocation id = Registry.ITEM.getKey(stack.getItem());
        for (String e : entries) {
            if (e.startsWith("#")) {
                ResourceLocation tag = ResourceLocation.tryParse(e.substring(1));
                if (tag != null && stack.is(TagKey.create(Registry.ITEM_REGISTRY, tag))) return true;
            } else if (e.endsWith(":*")) {
                if (id.getNamespace().equals(e.substring(0, e.length() - 2))) return true;
            } else if (e.equals(id.toString())) {
                return true;
            }
        }
        return false;
    }

    public static List<KeyMapping> aimKeys() {
        return AIM_KEYS.resolve(get(SsrCameraFixesConfig.AIM_KEYS, List.of()));
    }

    public static List<KeyMapping> faceCameraKeys() {
        return FACE_CAMERA_KEYS.resolve(get(SsrCameraFixesConfig.FACE_CAMERA_KEYS, List.of()));
    }

    private static final class KeyList {
        private List<? extends String> names;
        private int count = -1;
        private List<KeyMapping> keys = List.of();

        List<KeyMapping> resolve(List<? extends String> wanted) {
            KeyMapping[] all = Minecraft.getInstance().options.keyMappings;
            if (wanted != names || all.length != count) {
                List<KeyMapping> found = new ArrayList<>();
                for (KeyMapping k : all) {
                    for (String n : wanted) {
                        if (n.endsWith("*") ? k.getName().startsWith(n.substring(0, n.length() - 1)) : k.getName().equals(n)) {
                            found.add(k);
                            break;
                        }
                    }
                }
                keys = found;
                names = wanted;
                count = all.length;
            }
            return keys;
        }
    }

    public static <T> T get(ForgeConfigSpec.ConfigValue<T> value, @Nullable T fallback) {
        try {
            return value.get();
        } catch (Throwable t) {
            return fallback;
        }
    }
}
