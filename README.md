# Shoulder Surfing Reloaded: Camera Fixes & Additions

![Showcase](assets/showcase.gif)

Camera fixes and additions for [Shoulder Surfing Reloaded](https://www.curseforge.com/minecraft/mc-mods/shoulder-surfing-reloaded). Works on its own. Detects Epic Fight, Better Combat, Better Lockon, Iron's Spells, Spell Engine, and TaCZ when those mods are installed, and aims guns, wands and spell keys from other mods at the crosshair (see [Items and keys from other mods](#items-and-keys-from-other-mods)).

## Loaders

| Loader | Minecraft | Mod version | Integrations |
|---|---|---|---|
| Forge | 1.20.1 | 2.2.0 | Epic Fight, Better Combat, Better Lockon, Iron's Spells, Iron's Arms 'n Artifice, TaCZ, Radial Aggro Indicator, Valkyrien Skies, Combat Focus, Cinematic Interactions, Small Ships, Better Third Person |
| Forge | 1.19.2 | 1.2.0 | Epic Fight, Better Combat, Iron's Spells, TaCZ, Weapons of Miracle |
| NeoForge | 1.21.1 | 2.2.0 | Epic Fight, Better Combat, Better Lockon, Iron's Spells, Iron's Arms 'n Artifice, TaCZ, Confluence: Otherworld, Radial Aggro Indicator, Create: Aeronautics (Sable), Cinematic Interactions, Small Ships |
| Fabric | 1.20.1 | 2.2.0 | Better Combat, Spell Engine |
| Fabric | 1.21.1 | 2.2.0 | Better Combat, Spell Engine |

## Forge 1.20.1

Overhead preset in SSR's cycle, similar to Leawind's Third Person. One keybind: right shoulder, left shoulder, overhead. SSR's presets are per-axis and can't do a centered high overhead on their own, so this adds one.

On attack, your body and head turn with the camera so swings look forward and hits line up with the crosshair. Vanilla, modded, and Better Combat weapons. By default this follows SSR's "turn player when attacking" setting, so with SSR's default (requires target) swinging at air doesn't turn you. Set `turnOnAttack = "ALWAYS"` to turn on every swing.

- Stabilizes shoulder offset transitions, pitch, and SSR camera during and after lock-on
- Lock-on camera follows Epic Fight's locked target
- Hides the vanilla crosshair during Epic Fight lock-on (SSR adaptive crosshair only)
- Sprinting backward while locked on doesn't spin the camera behind you
- Body follows the crosshair while casting Iron's Spells, aiming a bow, eating, or blocking in SSR decoupled mode
- TaCZ: shots hit the crosshair when you ADS, hip-fire, or full-auto, and holding a gun no longer locks your body to the crosshair
- Epic Fight mover skills (Phantom Ascent, Demolition Leap, dodge) launch toward the crosshair instead of straight ahead
- Wall climb (WoM spider techniques): body doesn't twist with the camera or clip into the wall
- Radial Aggro Indicator: the aggro arrow points relative to the camera, not your body
- Valkyrien Skies: riding a mount on a ship keeps the shoulder offset and the camera follows your mouse, even when the ship is tilted (replaces the standalone ssr-vs-compat). Bows, tridents and thrown items fired from a seat go where you aim after the ship turns
- Iron's Arms 'n Artifice: guns fire at the crosshair
- Combat Focus: locking on in the overhead preset keeps the target under the crosshair, and its lock-on key no longer also cycles the shoulder when both are on O
- Cinematic Interactions: after a crafting scene you're back in shoulder surfing
- Small Ships: your body faces the bow while you sail, instead of turning with the camera
- Better Third Person: no startup warning from SSR, and Better Third Person's camera stays out of SSR's shoulder view (it used to keep the view pointed one way while you moved the mouse). F5 goes through both mods' cameras while SSR's `replace_default_perspective` is off (its default)
- Toggle to stop SSR's idle camera follow when you're not moving the mouse
- Shoulder cycle won't double-fire if it shares a key with SSR's swap-shoulder bind

### Requires

- Minecraft 1.20.1, Forge 47+, Shoulder Surfing Reloaded 5.0+
- Optional: Epic Fight 20.14.1+, Better Combat, Better Lockon, Iron's Spells, TaCZ, Weapons of Miracle

### Install

1. Forge 47+ on 1.20.1, plus Shoulder Surfing Reloaded.
2. Jar from [releases](https://github.com/Seramicx/ssr-camera-fixes/releases/latest) into `mods/`.

## Forge 1.19.2

Same set of fixes as Forge 1.20.1, minus Better Lockon, Wizards, and Radial Aggro Indicator (not available on 1.19.2). Iron's Spells and TaCZ behave the same as on 1.20.1. Epic Fight attacks turn your body to the crosshair, and Phantom Ascent launches toward the crosshair.

### Requires

- Minecraft 1.19.2, Forge 43+, Shoulder Surfing Reloaded 1.19.2-4.17.0+
- Optional: Epic Fight 19+, Better Combat, Iron's Spells, TaCZ, Weapons of Miracle

### Install

1. Forge 43+ on 1.19.2, plus Shoulder Surfing Reloaded.
2. Jar from [releases](https://github.com/Seramicx/ssr-camera-fixes/releases/latest) into `mods/`.

## NeoForge 1.21.1

Same as Forge on 1.21.1 / NeoForge 21, without Wizards. NeoForge 21.1+, SSR 5.0+. Optional: Antikythera-Studios Epic Fight 21.17.2+, Better Combat, Better Lockon, Iron's Spells, TaCZ (MUKSC's NeoForge 1.21.1 port), Confluence: Otherworld, Radial Aggro Indicator.

TaCZ and Confluence guns and mana weapons hit the crosshair, holding a TaCZ gun keeps decoupled movement, and the Radial Aggro Indicator arrow points relative to the camera. Iron's Arms 'n Artifice, Cinematic Interactions and Small Ships work the same as on Forge.

With Create: Aeronautics (and the Sable physics it runs on), F5 while sitting on a contraption goes through Sable's two contraption views right after shoulder surfing, and getting off puts you back in shoulder surfing.

### Install

1. NeoForge 21.1+ on 1.21.1, plus Shoulder Surfing Reloaded.
2. Matching jar from [releases](https://github.com/Seramicx/ssr-camera-fixes/releases) into `mods/`.

## Fabric 1.20.1 and 1.21.1

Overhead cycle, face-camera on Better Combat attacks, sprint-backward body lock, and Spell Engine spells aim at the crosshair (instant casts like Shadowstep included) while your character turns to face them. Fabric Loader 0.15+ (1.20.1) or 0.16+ (1.21.1), Fabric API, SSR (Fabric) 5.0+, Forge Config API Port (Fabric). Optional: Better Combat, Spell Engine (the RPG Series mods: Wizards, Rogues, Paladins, etc.).

### Install

1. Fabric Loader, Fabric API, Forge Config API Port, SSR (Fabric).
2. Matching Fabric jar from [releases](https://github.com/Seramicx/ssr-camera-fixes/releases) into `mods/`.

## Items and keys from other mods

SSR already turns you to the crosshair for vanilla right-click items. Many modded guns and wands fire from a keybind or send their own packet, so the shot leaves in the direction your body faced instead of where the crosshair is. This mod turns you to the crosshair right before those are sent, for any mod, without needing a specific integration:

- Right-click items that aren't blocks or food (`aimAllItems`, on by default)
- Items you list in `aimItems`, or items that shoot on left click in `aimOnAttackItems`
- Keybinds you list in `aimKeys`, such as a spell or ability key

If something from another mod still fires the wrong way, add it in game. Hold the item and type:

- `/ssrfix aim` - turn to the crosshair when you use it
- `/ssrfix aim attack` - same, for items that shoot on left click
- `/ssrfix ignore` - never turn for this item

Put `mod` on the end (`/ssrfix aim mod`) to cover every item from that mod. For a key, type `/ssrfix aimkey` (spells, abilities) or `/ssrfix dashkey` (dash, roll, dodge) and press it. You can also type the key after the command: Tab suggests keybinds and matches the names shown in Controls, which also works for keys that aren't bound yet. `/ssrfix list` shows what you've added (click an entry to remove it), and `/ssrfix remove` takes the held item off every list. Changes are saved to the config and work straight away.

On NeoForge and Fabric 1.21.1 these settings are also in a config screen (the Mods button on NeoForge, Mod Menu on Fabric).

In `config/ssrcamerafixes-client.toml`, keys use their name from `options.txt` without the `key_` prefix, and a trailing `*` matches every key starting with that text. Entries can be an item id (`goety:soul_staff`), a whole mod (`goety:*`) or an item tag (`#forge:tools`). `ignoredItems` turns aiming off for an item or mod; TaCZ and Superb Warfare are in it by default because their integrations handle aiming.

A data pack can do the same with the item tags `ssrcamerafixes:aim_at_crosshair`, `ssrcamerafixes:aim_at_crosshair_on_attack` and `ssrcamerafixes:never_aim`. Tags come from the server, so on a multiplayer server the data pack has to be installed there; the config lists work on any server.

Aiming follows SSR's "turn player when using item" setting unless you change `turnOnUse`. It is skipped during free look and during Epic Fight lock-on, where Epic Fight turns you toward the locked target.

Dash, roll and dodge keys from other mods launch you the way your body faces, which in shoulder surfing is often not where you are looking. Add those keys with `/ssrfix dashkey` (or put them in `faceCameraKeys`) and your character turns to the camera direction before the key fires.

## Config

`config/ssrcamerafixes-client.toml`:

- `cameraOverheadOffsetY` - overhead height (default `1.2`). X locked to 0 in overhead; Z comes from SSR's `offset_z`.
- `disableFollowPlayerRotations` - turns off SSR idle camera follow.
- `decoupleHeldCrossbow` - keeps the camera decoupled while a loaded crossbow is held (default `false`, SSR's stock coupling).
- `turnOnAttack`, `turnOnUse` - `FOLLOW_SSR` (default) uses SSR's "turn player when attacking" and "turn player when using item" settings. `ALWAYS` and `NEVER` override them for this mod's turning.
- `aimAllItems`, `aimItems`, `aimOnAttackItems`, `ignoredItems`, `aimKeys`, `faceCameraKeys` - see [Items and keys from other mods](#items-and-keys-from-other-mods).
- `attackBodySnap` - body and head follow the swing when you attack.
- `aimingBodyFollow` - body follows the crosshair while drawing a bow, casting, eating or blocking.
- `restoreShoulderSurfing` - switches back to shoulder surfing when another mod drops you into plain third person without you pressing a perspective key (default `false`). Leave it off if a mod uses plain third person on purpose for its own camera.
- `[compat]` - one switch per supported mod (`epicfight`, `tacz`, `cgm`, ...). Setting one to `false` makes this mod leave that mod alone. Restart the game after changing these.

Other offsets live in `config/shouldersurfing-client.toml`.

## Keybinds

- Shoulder Cycle - default `O`. Right shoulder -> left shoulder -> overhead -> right.

The side is not saved. It starts each session on whichever shoulder SSR's own `offset_x` points at, so you can edit `shouldersurfing-client.toml` while the game is running without this mod overwriting it.

TaCZ defaults its "Interact With Gun" key to `O` too, and that key opens the gun smith table. Rebind one of them in Controls if you run both.

## License

MIT
