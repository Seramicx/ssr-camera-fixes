# Shoulder Surfing Reloaded: Camera Fixes & Additions

![Showcase](assets/showcase.gif)

Camera fixes and additions for [Shoulder Surfing Reloaded](https://www.curseforge.com/minecraft/mc-mods/shoulder-surfing-reloaded). Works on its own. Detects Epic Fight, Better Combat, Better Lockon, Iron's Spells, Wizards, and TaCZ when those mods are installed.

## Loaders

| Loader | Minecraft | Mod version | Integrations |
|---|---|---|---|
| Forge | 1.20.1 | 1.2.6 | Epic Fight, Better Combat, Better Lockon, Iron's Spells, Wizards, TaCZ |
| Forge | 1.19.2 | 1.0.0 | Epic Fight, Better Combat, Iron's Spells, TaCZ |
| NeoForge | 1.21.1 | 1.0.3 | Epic Fight, Better Combat, Better Lockon, Iron's Spells |
| Fabric | 1.20.1 | 1.0.2 | Better Combat, Wizards |
| Fabric | 1.21.1 | 1.0.1 | Better Combat, Wizards |

## Forge 1.20.1

Overhead preset in SSR's cycle, similar to Leawind's Third Person. One keybind: right shoulder, left shoulder, overhead. SSR's presets are per-axis and can't do a centered high overhead on their own, so this adds one.

On attack, your body and head turn with the camera so swings look forward and hits line up with the crosshair. Vanilla, modded, and Better Combat weapons.

- Stabilizes shoulder offset transitions, pitch, and SSR camera during and after lock-on
- Lock-on camera follows Epic Fight's locked target
- Hides the vanilla crosshair during Epic Fight lock-on (SSR adaptive crosshair only)
- Sprinting backward while locked on doesn't spin the camera behind you
- Body follows the crosshair while casting Iron's Spells or Wizards spells, aiming a bow, eating, or blocking in SSR decoupled mode
- TaCZ: shots hit the crosshair when you ADS, hip-fire, or full-auto
- Wall climb (WoM spider techniques): body doesn't twist with the camera or clip into the wall
- Toggle to stop SSR's idle camera follow when you're not moving the mouse
- Shoulder cycle won't double-fire if it shares a key with SSR's swap-shoulder bind

### Requires

- Minecraft 1.20.1, Forge 47+, Shoulder Surfing Reloaded 4.22.0+
- Optional: Epic Fight 20.14.1+, Better Combat, Better Lockon, Iron's Spells, Wizards, TaCZ, Weapons of Miracle

### Install

1. Forge 47+ on 1.20.1, plus Shoulder Surfing Reloaded.
2. Jar from [releases](https://github.com/Seramicx/ssr-camera-fixes/releases/latest) into `mods/`.

## Forge 1.19.2

Same set of fixes as Forge 1.20.1, minus Better Lockon and Wizards (not available on 1.19.2). EpicFight lock-on is detected through `LocalPlayerPatch.isTargetLockedOn()` since the 1.20.1 `EpicFightCameraAPI` doesn't exist on 1.19. Iron's Spells and TaCZ behave the same as on 1.20.1.

### Requires

- Minecraft 1.19.2, Forge 43+, Shoulder Surfing Reloaded 1.19.2-4.17.0+
- Optional: Epic Fight 19+, Better Combat, Iron's Spells, TaCZ, Weapons of Miracle

### Install

1. Forge 43+ on 1.19.2, plus Shoulder Surfing Reloaded.
2. Jar from [releases](https://github.com/Seramicx/ssr-camera-fixes/releases/latest) into `mods/`.

## NeoForge 1.21.1

Same as Forge on 1.21.1 / NeoForge 21, without Wizards or TaCZ yet. NeoForge 21.1+, SSR 4.22.10+. Optional: Antikythera-Studios Epic Fight 21.17.2+, Better Combat, Better Lockon, Iron's Spells.

### Install

1. NeoForge 21.1+ on 1.21.1, plus Shoulder Surfing Reloaded.
2. Matching jar from [releases](https://github.com/Seramicx/ssr-camera-fixes/releases) into `mods/`.

## Fabric 1.20.1 and 1.21.1

Overhead cycle, face-camera on Better Combat attacks, sprint-backward body lock, Wizards cast body-follow. Fabric Loader 0.15+ (1.20.1) or 0.16+ (1.21.1), Fabric API, SSR (Fabric), Forge Config API Port (Fabric). Optional: Better Combat, Wizards.

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
