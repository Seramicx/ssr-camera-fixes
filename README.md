# Shoulder Surfing Reloaded: Camera Fixes & Additions

![Showcase](assets/showcase.gif)

Camera fixes and additions for [Shoulder Surfing Reloaded](https://www.curseforge.com/minecraft/mc-mods/shoulder-surfing-reloaded). Available for Forge 1.20.1, Fabric 1.20.1, and NeoForge 1.21.1. Standalone. Integrations with Epic Fight, Better Combat, Better Lockon, and Iron's Spells are auto-detected when those mods are present.

## Loaders

| Loader | Minecraft | Mod version | Integrations |
|---|---|---|---|
| Forge | 1.20.1 | 1.2.2 | Epic Fight, Better Combat, Better Lockon, Iron's Spells |
| NeoForge | 1.21.1 | 1.0.0 | Epic Fight, Better Combat, Better Lockon, Iron's Spells |
| Fabric | 1.20.1 | 1.0.0 | Better Combat |

## Forge 1.20.1

### Features

- Configurable overhead preset added to the SSR preset cycle to mimic Leawind's Third Person. Single keybind cycles right shoulder, left shoulder, overhead. SSR's own preset system is per-axis and can't represent a coupled "centered + high" overhead, so this layers one on top via SSR's plugin API.
- Auto face camera on attack. While you're swinging, the body and head match `player.yRot` so the body fully turns with the swing instead of getting stuck partway, the swing animation reads as forward, and the hitbox lands where the crosshair points. Works for any weapon, vanilla, modded, or Better Combat, for the full swing duration with no hardcoded timing.
- Smoothly transitions between shoulder presets when you cycle them while locked on, instead of snapping. (Lock-on integration: Epic Fight + Better Lockon.)
- Keeps the SSR shoulder offset stable during lock-on, so the EF + BLO + SSR stack doesn't zero the lateral shoulder shift mid-lock and recenter the camera on the player.
- Hides the vanilla crosshair while you're locked on with Epic Fight, so you only see SSR's adaptive crosshair instead of two crosshairs at once.
- No camera-recenter wobble after releasing lock-on. The camera stays exactly where the shoulder offset put it.
- Sprinting backwards while locked on no longer makes the camera chase your movement direction. The camera stays on the enemy as it should.
- Body actually follows the crosshair while casting Iron's Spells, aiming a bow, eating, or blocking in SSR decoupled mode, so projectiles and actions land where you're aiming instead of where your body happens to be facing.
- Wall-climbing (WOM spider techniques): your character no longer twists with the camera while running up a wall, and can no longer clip into the wall when you look around.
- Idle camera follow option: the camera no longer auto-rotates to face your direction when you haven't moved the mouse for a few seconds. Toggle in the config if you want SSR's stock behavior back.
- Shoulder cycle / SSR keybind dedup. If you've bound shoulder cycle to the same key SSR uses for its own swap-shoulder (both default to O), only one swap fires per press.

### Requires

- Minecraft 1.20.1
- Forge 47+
- Shoulder Surfing Reloaded 4.22.0+

Optional, auto-detected when present:

- Epic Fight 20.14.1+ (lock-on integrations: crosshair hide, offset preservation, smooth preset transitions, sprint-backwards lock, attack-on-target body align).
- Better Combat (auto face camera on attack covers BC's upswing + downswing for any weapon).
- Better Lockon (lock-on integration with EF).
- Iron's Spells 'n Spellbooks (continuous facing during casts).
- WOM spider techniques (wall-climb body lock).

### Install

1. Install Forge 47+ for Minecraft 1.20.1.
2. Install Shoulder Surfing Reloaded.
3. Download the Forge jar from the [latest release](https://github.com/Seramicx/ssr-camera-fixes/releases/latest).
4. Drop it into your `.minecraft/mods/` folder.

## NeoForge 1.21.1

### Features

Same set as the Forge build, on Minecraft 1.21.1 / NeoForge 21.

### Requires

- Minecraft 1.21.1
- NeoForge 21.1+
- Shoulder Surfing Reloaded 4.22.10+

Optional, auto-detected when present:

- Epic Fight 21.17.2+ (Antikythera-Studios fork)
- Better Combat
- Better Lockon
- Iron's Spells 'n Spellbooks

### Install

1. Install NeoForge 21.1+ for Minecraft 1.21.1.
2. Install Shoulder Surfing Reloaded.
3. Download the NeoForge jar from the [latest release](https://github.com/Seramicx/ssr-camera-fixes/releases).
4. Drop it into your `.minecraft/mods/` folder.

## Fabric 1.20.1

### Features

- Configurable overhead preset added to the SSR preset cycle. Single keybind cycles right shoulder, left shoulder, overhead.
- Auto face camera on attack covering Better Combat's full swing window.
- Sprint-backwards body lock so the camera stops chasing your movement direction while sprinting in third person.

### Requires

- Minecraft 1.20.1
- Fabric Loader 0.14.21+
- Fabric API
- Shoulder Surfing Reloaded (Fabric)
- Forge Config API Port (Fabric)

Optional, auto-detected when present:

- Better Combat

### Install

1. Install Fabric Loader for Minecraft 1.20.1.
2. Install Fabric API, Forge Config API Port, and Shoulder Surfing Reloaded.
3. Download the Fabric jar from the [latest release](https://github.com/Seramicx/ssr-camera-fixes/releases).
4. Drop it into your `.minecraft/mods/` folder.

## Config

`config/ssrcamerafixes-client.toml`:

- `cameraOverheadOffsetY` - vertical Y offset for the overhead preset (default `1.2`). The only value SSR's preset system can't represent on its own. X is forced to 0 in overhead, Z is inherited from SSR's `offset_z` setting.
- `disableFollowPlayerRotations` - when true, SSR's idle camera follow is suppressed so the camera no longer auto-rotates toward your facing after a few seconds.

Right / left X offsets, vertical Y, and back distance Z come from SSR's own `config/shouldersurfing-client.toml`.

## Keybinds

- Shoulder Cycle - default `O`. Cycles right shoulder -> left shoulder -> overhead -> right.

## License

MIT
