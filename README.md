# Shoulder Surfing Reloaded: Camera Fixes & Additions

A handful of camera fixes and additions for [Shoulder Surfing Reloaded](https://www.curseforge.com/minecraft/mc-mods/shoulder-surfing-reloaded) when used with Epic Fight, Better Lockon, and Iron's Spells on Forge 1.20.1.

## Features

- Shoulder cycle keybind. Press O to cycle right shoulder -> left shoulder -> overhead -> right. SSR's own preset system is per-axis and can't represent a coupled "X=0 + high Y" overhead, so this layers one on top via SSR's plugin API.
- Hide the vanilla crosshair during Epic Fight lock-on. SSR draws its own adaptive crosshair, so without this you'd get both at the same time.
- Keep SSR's shoulder offset during lock-on. With the full EF + BLO + SSR stack, `targetOffset.x` was being zeroed mid-lock (camera centering on the player instead of staying off-shoulder). This pins the offset to your configured value for the whole lock-on.
- Smooth preset transitions during lock-on. Cycling right / left / overhead while locked eases between them instead of snapping.
- No camera-recenter after lock-off. Without the offset pin above, SSR's smoothed `offset` would drift to zeroed-X during lock-on and then take ~2s to lerp back. With the pin, there's nothing to lerp back.
- Sprint-backwards camera lock. S+D while sprinting and locked on used to make the camera chase your movement direction instead of staying on the enemy. Cancels Epic Fight's `ShoulderSurfingCompat.lockOnTick` which was overwriting BLO's per-tick yRot lerp.
- Continuous facing during Iron's Spells casts. With SSR decoupled, the player's body wasn't following the crosshair during a cast, so projectiles spawned at the body angle instead. Drives `player.yRot` toward the camera direction throughout the cast window.
- Shoulder cycle / SSR keybind dedup. If you bind shoulder cycle to the same key SSR uses for its own swap-shoulder (both default to O), this drains SSR's click queue so only one swap fires per press.

## Requirements

- Minecraft 1.20.1 + Forge 47+
- [Shoulder Surfing Reloaded](https://www.curseforge.com/minecraft/mc-mods/shoulder-surfing-reloaded) 4.22.0+
- [Epic Fight](https://www.curseforge.com/minecraft/mc-mods/epic-fight) 20.14.1+

## Optional companions

- [Better Lockon](https://www.curseforge.com/minecraft/mc-mods/better-lockon)
- [Iron's Spells 'n Spellbooks](https://www.curseforge.com/minecraft/mc-mods/irons-spells-n-spellbooks)

## Config

`config/ssrcamerafixes-client.toml`:

- `cameraOverheadOffsetY` - vertical Y for the OVERHEAD preset (default 1.2). The only value SSR's preset system can't represent on its own. X is forced to 0 in overhead, Z is inherited from SSR's `offset_z`.

Other values (right/left X, vertical Y, back distance Z) come from SSR's own config at `config/shouldersurfing-client.toml`.

## Keybinds

- Shoulder Cycle (default O).

## License

MIT
