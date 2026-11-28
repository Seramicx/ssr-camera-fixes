package com.ssrcamerafixes.command;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.ssrcamerafixes.SsrCameraFixesConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class ConfigCommands {
    public static final ConfigCommands INSTANCE = new ConfigCommands();

    @Nullable private ModConfigSpec.ConfigValue<List<? extends String>> capturing;
    private int captureTicks;

    private ConfigCommands() {
    }

    private static Map<String, ModConfigSpec.ConfigValue<List<? extends String>>> lists() {
        Map<String, ModConfigSpec.ConfigValue<List<? extends String>>> m = new LinkedHashMap<>();
        m.put("aimItems", SsrCameraFixesConfig.AIM_ITEMS);
        m.put("aimOnAttackItems", SsrCameraFixesConfig.AIM_ON_ATTACK_ITEMS);
        m.put("ignoredItems", SsrCameraFixesConfig.IGNORED_ITEMS);
        m.put("aimKeys", SsrCameraFixesConfig.AIM_KEYS);
        m.put("faceCameraKeys", SsrCameraFixesConfig.FACE_CAMERA_KEYS);
        return m;
    }

    private static <S> LiteralArgumentBuilder<S> lit(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    private static <S> RequiredArgumentBuilder<S, String> text(String name) {
        return RequiredArgumentBuilder.argument(name, StringArgumentType.greedyString());
    }

    public <S> LiteralArgumentBuilder<S> build() {
        LiteralArgumentBuilder<S> root = lit("ssrfix");
        root.executes(c -> help());
        root.then(ConfigCommands.<S>lit("aim").executes(c -> addHeld("aimItems", false))
                .then(ConfigCommands.<S>lit("mod").executes(c -> addHeld("aimItems", true)))
                .then(ConfigCommands.<S>lit("attack").executes(c -> addHeld("aimOnAttackItems", false))
                        .then(ConfigCommands.<S>lit("mod").executes(c -> addHeld("aimOnAttackItems", true)))));
        root.then(ConfigCommands.<S>lit("ignore").executes(c -> addHeld("ignoredItems", false))
                .then(ConfigCommands.<S>lit("mod").executes(c -> addHeld("ignoredItems", true))));
        root.then(ConfigCommands.<S>lit("remove").executes(c -> removeHeld(false))
                .then(ConfigCommands.<S>lit("mod").executes(c -> removeHeld(true))));
        root.then(keyCommand("aimkey", "aimKeys"));
        root.then(keyCommand("dashkey", "faceCameraKeys"));
        root.then(ConfigCommands.<S>lit("forget").then(ConfigCommands.<S>text("entry")
                .suggests((c, b) -> suggestEntries(b))
                .executes(c -> forget(StringArgumentType.getString(c, "entry")))));
        root.then(ConfigCommands.<S>lit("list").executes(c -> list()));
        return root;
    }

    private <S> LiteralArgumentBuilder<S> keyCommand(String name, String list) {
        return ConfigCommands.<S>lit(name).executes(c -> startCapture(list))
                .then(ConfigCommands.<S>text("key")
                        .suggests((c, b) -> suggestKeys(b))
                        .executes(c -> addKey(list, StringArgumentType.getString(c, "key"))));
    }

    private int help() {
        say(Component.literal("Shoulder Surfing camera fixes, hold an item or pick a key:").withStyle(ChatFormatting.GOLD));
        say(Component.literal("/ssrfix aim - this item turns you to the crosshair when you use it"));
        say(Component.literal("/ssrfix aim attack - same, for items that shoot on left click"));
        say(Component.literal("/ssrfix ignore - never turn for this item"));
        say(Component.literal("Add 'mod' to any of those for every item from the same mod"));
        say(Component.literal("/ssrfix aimkey - press a spell or ability key to make it aim"));
        say(Component.literal("/ssrfix dashkey - press a dash or dodge key to make it go where you look"));
        say(Component.literal("/ssrfix remove, /ssrfix forget <entry>, /ssrfix list"));
        return 1;
    }

    private int addHeld(String list, boolean wholeMod) {
        ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
        if (stack.isEmpty()) {
            fail("Hold the item in your main hand first.");
            return 0;
        }
        ResourceLocation id = stack.getItem().builtInRegistryHolder().key().location();
        String entry = wholeMod ? id.getNamespace() + ":*" : id.toString();
        for (String other : List.of("aimItems", "aimOnAttackItems", "ignoredItems")) {
            if (!other.equals(list) && (list.equals("ignoredItems") || other.equals("ignoredItems"))) {
                remove(lists().get(other), entry);
            }
        }
        boolean added = add(lists().get(list), entry);
        String what = wholeMod ? "Every " + id.getNamespace() + " item" : stack.getHoverName().getString();
        String effect = switch (list) {
            case "aimItems" -> " now turns you to the crosshair when used.";
            case "aimOnAttackItems" -> " now turns you to the crosshair when you left click.";
            default -> " is now left alone.";
        };
        say(Component.literal(what + effect).withStyle(ChatFormatting.GREEN)
                .append(Component.literal(added ? "" : " (it was already on the list)").withStyle(ChatFormatting.GRAY)));
        if (!list.equals("ignoredItems") && !wholeMod && contains(SsrCameraFixesConfig.IGNORED_ITEMS, id.getNamespace() + ":*")) {
            say(Component.literal(id.getNamespace() + ":* is in ignoredItems and still wins. Use /ssrfix forget " + id.getNamespace() + ":* to change that.").withStyle(ChatFormatting.YELLOW));
        }
        return 1;
    }

    private int removeHeld(boolean wholeMod) {
        ItemStack stack = Minecraft.getInstance().player.getMainHandItem();
        if (stack.isEmpty()) {
            fail("Hold the item in your main hand first.");
            return 0;
        }
        ResourceLocation id = stack.getItem().builtInRegistryHolder().key().location();
        return forget(wholeMod ? id.getNamespace() + ":*" : id.toString());
    }

    private int forget(String entry) {
        List<String> from = new ArrayList<>();
        lists().forEach((name, value) -> {
            if (remove(value, entry)) from.add(name);
        });
        if (from.isEmpty()) {
            fail(entry + " isn't on any list.");
            return 0;
        }
        say(Component.literal("Removed " + entry + " from " + String.join(", ", from) + ".").withStyle(ChatFormatting.GREEN));
        return 1;
    }

    private int list() {
        lists().forEach((name, value) -> {
            MutableComponent line = Component.literal(name + ": ").withStyle(ChatFormatting.GOLD);
            List<? extends String> entries = value.get();
            if (entries.isEmpty()) line.append(Component.literal("(empty)").withStyle(ChatFormatting.GRAY));
            for (int i = 0; i < entries.size(); i++) {
                String e = entries.get(i);
                if (i > 0) line.append(Component.literal(", ").withStyle(ChatFormatting.WHITE));
                line.append(Component.literal(e).withStyle(Style.EMPTY.withColor(ChatFormatting.WHITE)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/ssrfix forget " + e))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to remove")))));
            }
            say(line);
        });
        return 1;
    }

    private int startCapture(String list) {
        capturing = lists().get(list);
        captureTicks = 0;
        say(Component.literal("Press the key you want to add to " + list + " (10 seconds).").withStyle(ChatFormatting.YELLOW));
        return 1;
    }

    private int addKey(String list, String name) {
        if (!name.endsWith("*") && findKey(name) == null) {
            fail("No keybind called " + name + ". Press Tab while typing to see them.");
            return 0;
        }
        boolean added = add(lists().get(list), name);
        KeyMapping key = findKey(name);
        MutableComponent label = key != null ? Component.translatable(key.getName()) : Component.literal(name);
        say(Component.literal("").append(label).append(Component.literal(" (" + name + ")" + (list.equals("aimKeys")
                        ? " now turns you to the crosshair when pressed."
                        : " now turns you to face where you look before it fires.")))
                .withStyle(ChatFormatting.GREEN)
                .append(Component.literal(added ? "" : " (it was already on the list)").withStyle(ChatFormatting.GRAY)));
        return 1;
    }

    public void clientTick() {
        if (capturing != null && ++captureTicks > 200) {
            capturing = null;
            if (Minecraft.getInstance().player != null) fail("No key pressed, nothing added.");
        }
    }

    public void onKeyClick(InputConstants.Key key) {
        Minecraft mc = Minecraft.getInstance();
        if (capturing == null || mc.screen != null || mc.player == null) return;
        List<KeyMapping> pressed = new ArrayList<>();
        for (KeyMapping k : mc.options.keyMappings) {
            if (k.saveString().equals(key.getName())) pressed.add(k);
        }
        if (pressed.isEmpty()) return;
        String list = lists().entrySet().stream().filter(e -> e.getValue() == capturing).findFirst().map(Map.Entry::getKey).orElse("aimKeys");
        capturing = null;
        if (pressed.size() == 1) {
            addKey(list, pressed.get(0).getName());
            return;
        }
        String command = list.equals("aimKeys") ? "aimkey" : "dashkey";
        say(Component.literal("That key does several things. Click the one you meant:").withStyle(ChatFormatting.YELLOW));
        for (KeyMapping k : pressed) {
            say(Component.literal("  ").append(Component.translatable(k.getName()))
                    .append(Component.literal(" (" + k.getName() + ")"))
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/ssrfix " + command + " " + k.getName()))));
        }
    }

    private static CompletableFuture<Suggestions> suggestKeys(SuggestionsBuilder b) {
        String typed = b.getRemaining().toLowerCase(Locale.ROOT);
        for (KeyMapping k : Minecraft.getInstance().options.keyMappings) {
            String shown = Component.translatable(k.getName()).getString();
            if (k.getName().toLowerCase(Locale.ROOT).contains(typed) || shown.toLowerCase(Locale.ROOT).contains(typed)) {
                b.suggest(k.getName(), Component.literal(shown + " [" + k.getTranslatedKeyMessage().getString() + "]"));
            }
        }
        return b.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestEntries(SuggestionsBuilder b) {
        String typed = b.getRemaining().toLowerCase(Locale.ROOT);
        Set<String> seen = new HashSet<>();
        lists().forEach((name, value) -> {
            for (String e : value.get()) {
                if (e.toLowerCase(Locale.ROOT).contains(typed) && seen.add(e)) b.suggest(e, Component.literal(name));
            }
        });
        return b.buildFuture();
    }

    @Nullable
    private static KeyMapping findKey(String name) {
        for (KeyMapping k : Minecraft.getInstance().options.keyMappings) {
            if (k.getName().equals(name)) return k;
        }
        return null;
    }

    private static boolean contains(ModConfigSpec.ConfigValue<List<? extends String>> value, String entry) {
        return value.get().contains(entry);
    }

    private static boolean add(ModConfigSpec.ConfigValue<List<? extends String>> value, String entry) {
        List<String> l = new ArrayList<>(value.get());
        if (l.contains(entry)) return false;
        l.add(entry);
        value.set(l);
        value.save();
        return true;
    }

    private static boolean remove(ModConfigSpec.ConfigValue<List<? extends String>> value, String entry) {
        List<String> l = new ArrayList<>(value.get());
        if (!l.remove(entry)) return false;
        value.set(l);
        value.save();
        return true;
    }

    private static void say(Component message) {
        Minecraft.getInstance().player.displayClientMessage(message, false);
    }

    private static void fail(String message) {
        say(Component.literal(message).withStyle(ChatFormatting.RED));
    }
}
