# Shoulder Surfing Reloaded: Camera Fixes & Additions

A handful of camera fixes and additions for [Shoulder Surfing Reloaded](https://www.curseforge.com/minecraft/mc-mods/shoulder-surfing-reloaded) when used alongside Epic Fight, Better Lockon, and Iron's Spells. Forge 1.20.1.

## Features

- Adds a new configurable overhead preset to the SSR preset cycle to mimic Leawind's Third Person. Single keybind cycles right shoulder, left shoulder, overhead. SSR's own preset system is per-axis and can't represent a coupled "centered + high" overhead, so this layers one on top via SSR's plugin API.
- Hides the vanilla crosshair while you're locked on with Epic Fight, so you only see SSR's adaptive crosshair instead of two crosshairs at once.
- Keeps the SSR shoulder offset stable during lock-on. Without this, the EF + BLO + SSR stack would zero out the lateral shoulder shift mid-lock and recenter the camera on the player.
- Smoothly transitions between shoulder presets when you cycle them while locked on, instead of snapping.
- No camera-recenter wobble after releasing lock-on. The camera stays exactly where the shoulder offset put it.
- Sprinting backwards while locked on no longer makes the camera chase your movement direction. The camera stays on the enemy as it should.
- Body actually follows the crosshair while casting Iron's Spells in SSR decoupled mode, so projectiles spawn where you're aiming instead of where your body happens to be facing.
- Shoulder cycle / SSR keybind dedup. If you've bound shoulder cycle to the same key SSR uses for its own swap-shoulder (both default to O), only one swap fires per press.

## Config

`config/ssrcamerafixes-client.toml`:

- `cameraOverheadOffsetY` - vertical Y offset for the overhead preset (default `1.2`). The only value SSR's preset system can't represent on its own. X is forced to 0 in overhead, Z is inherited from SSR's `offset_z` setting.

Right / left X offsets, vertical Y, and back distance Z come from SSR's own `config/shouldersurfing-client.toml`.

## Keybinds

- Shoulder Cycle - default `O`. Cycles right shoulder -> left shoulder -> overhead -> right.

## Requires

- Minecraft 1.20.1
- Forge 47+
- Shoulder Surfing Reloaded 4.22.0+
- Epic Fight 20.14.1+

Better Lockon and Iron's Spells 'n Spellbooks are optional and auto-detected.

## Manual install

1. Install Forge 47+ for Minecraft 1.20.1.
2. Install Shoulder Surfing Reloaded and Epic Fight.
3. Download the jar from the [latest release](https://github.com/Seramicx/ssr-camera-fixes/releases/latest).
4. Drop it into your `.minecraft/mods/` folder.

## License

MIT
