# Shoulder Surfing Reloaded: Camera Fixes & Additions

![Showcase](assets/showcase.gif)

Camera fixes and additions for [Shoulder Surfing Reloaded](https://www.curseforge.com/minecraft/mc-mods/shoulder-surfing-reloaded). Works on its own. Detects Epic Fight, Better Combat, Better Lockon, Iron's Spells, Spell Engine, and TaCZ when those mods are installed.

## Loaders

| Loader | Minecraft | Mod version | Integrations |
|---|---|---|---|
| Forge | 1.20.1 | 1.2.9 | Epic Fight, Better Combat, Better Lockon, Iron's Spells, TaCZ, Radial Aggro Indicator, Valkyrien Skies |
| Forge | 1.19.2 | 1.0.0 | Epic Fight, Better Combat, Iron's Spells, TaCZ, Weapons of Miracle |
| NeoForge | 1.21.1 | 1.0.6 | Epic Fight, Better Combat, Better Lockon, Iron's Spells, TaCZ, Confluence: Otherworld, Radial Aggro Indicator, Create: Aeronautics (Sable) |
| Fabric | 1.20.1 | 1.0.4 | Better Combat, Spell Engine |
| Fabric | 1.21.1 | 1.0.3 | Better Combat, Spell Engine |

## Forge 1.20.1

Overhead preset in SSR's cycle, similar to Leawind's Third Person. One keybind: right shoulder, left shoulder, overhead. SSR's presets are per-axis and can't do a centered high overhead on their own, so this adds one.

On attack, your body and head turn with the camera so swings look forward and hits line up with the crosshair. Vanilla, modded, and Better Combat weapons.

- Stabilizes shoulder offset transitions, pitch, and SSR camera during and after lock-on
- Lock-on camera follows Epic Fight's locked target
- Hides the vanilla crosshair during Epic Fight lock-on (SSR adaptive crosshair only)
- Sprinting backward while locked on doesn't spin the camera behind you
- Body follows the crosshair while casting Iron's Spells, aiming a bow, eating, or blocking in SSR decoupled mode
- TaCZ: shots hit the crosshair when you ADS, hip-fire, or full-auto, and holding a gun no longer locks your body to the crosshair
- Epic Fight mover skills (Phantom Ascent, Demolition Leap, dodge) launch toward the crosshair instead of straight ahead
- Wall climb (WoM spider techniques): body doesn't twist with the camera or clip into the wall
- Radial Aggro Indicator: the aggro arrow points relative to the camera, not your body
- Valkyrien Skies: riding a mount on a ship keeps the shoulder offset and the camera follows your mouse, even when the ship is tilted (replaces the standalone ssr-vs-compat)
- Toggle to stop SSR's idle camera follow when you're not moving the mouse
- Shoulder cycle won't double-fire if it shares a key with SSR's swap-shoulder bind

### Requires

- Minecraft 1.20.1, Forge 47+, Shoulder Surfing Reloaded 4.22.0+
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

Same as Forge on 1.21.1 / NeoForge 21, without Wizards. NeoForge 21.1+, SSR 4.22.10+. Optional: Antikythera-Studios Epic Fight 21.17.2+, Better Combat, Better Lockon, Iron's Spells, TaCZ (MUKSC's NeoForge 1.21.1 port), Confluence: Otherworld, Radial Aggro Indicator.

TaCZ and Confluence guns and mana weapons hit the crosshair, holding a TaCZ gun keeps decoupled movement, and the Radial Aggro Indicator arrow points relative to the camera.

With Create: Aeronautics (and the Sable physics it runs on), sitting on a contraption no longer crashes or spams the log when you enter shoulder surfing, the contraption camera modes are still reachable on F5, and the camera stays steady on a tilted contraption instead of swinging around.

### Install

1. NeoForge 21.1+ on 1.21.1, plus Shoulder Surfing Reloaded.
2. Matching jar from [releases](https://github.com/Seramicx/ssr-camera-fixes/releases) into `mods/`.

## Fabric 1.20.1 and 1.21.1

Overhead cycle, face-camera on Better Combat attacks, sprint-backward body lock, and Spell Engine spells aim at the crosshair (instant casts like Shadowstep included) while your character turns to face them. Fabric Loader 0.15+ (1.20.1) or 0.16+ (1.21.1), Fabric API, SSR (Fabric), Forge Config API Port (Fabric). Optional: Better Combat, Spell Engine (the RPG Series mods: Wizards, Rogues, Paladins, etc.).

### Install

1. Fabric Loader, Fabric API, Forge Config API Port, SSR (Fabric).
2. Matching Fabric jar from [releases](https://github.com/Seramicx/ssr-camera-fixes/releases) into `mods/`.

## Bows and thrown items

Crosshair aim for bows, crossbows, tridents, and use-items is in [Epic Fight x Better Lock On: Movement Fixes](https://github.com/Seramicx/epic-fight-better-lockon-movement-camera-fix). Install both mods if you want camera fixes and that aim behavior.

## Config

`config/ssrcamerafixes-client.toml`:

- `cameraOverheadOffsetY` - overhead height (default `1.2`). X locked to 0 in overhead; Z comes from SSR's `offset_z`.
- `disableFollowPlayerRotations` - turns off SSR idle camera follow.

Other offsets live in `config/shouldersurfing-client.toml`.

## Keybinds

- Shoulder Cycle - default `O`. Right shoulder -> left shoulder -> overhead -> right.

## License

MIT
