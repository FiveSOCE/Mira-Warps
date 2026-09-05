# MiraWarps

MiraWarps is the EssentialsX-backed warp GUI for the Mira Paper server suite. It presents the server's existing Essentials warps through a clean visual menu while leaving Essentials authoritative for warp data, teleport rules and permissions.

## Download

[**Download MiraWarps v0.1.2**](https://github.com/FiveSOCE/Mira-Warps/releases/download/v0.1.2/MiraWarps-0.1.2.jar)

[View All Releases](https://github.com/FiveSOCE/Mira-Warps/releases)

## Requirements / Dependencies

- Paper 1.21.11
- Java 21
- EssentialsX 2.22.0 or newer
- MiraCosmetics 0.1.1+ optional/recommended for teleport visuals

## How MiraWarps Works

Warp names are read live from EssentialsX, so MiraWarps never maintains a separate warp database. The GUI expands and paginates as required.

Clicking a warp closes the GUI and dispatches Essentials' namespaced `/warp <name>` command. Essentials continues to enforce permission, delay, cooldown and teleport behavior.

v0.1.2 removes the old local three-ring particle renderer. When MiraCosmetics is installed, its global Paper `PlayerTeleportEvent` listener renders the player's configured TELEPORT cosmetic only after Essentials actually performs a teleport. Direct `/warp <name>` and GUI-selected warps therefore use the same visual pipeline.

The `/warp` and `/warps` command bridge routes no-argument use into the GUI, while `/warp <name>` remains normal Essentials direct warp behavior.

## Commands

| Command | Permission | What it does |
| --- | --- | --- |
| `/mwarps` | `mirawarps.use` | Opens the MiraWarps GUI. |
| `/mwarp` | `mirawarps.use` | Alias for `/mwarps`. |
| `/warp` | Essentials access + MiraWarps routing | Opens the GUI when used without a warp name. |
| `/warps` | Essentials access + MiraWarps routing | Opens the GUI. |
| `/warp <name>` | Essentials permissions | Remains EssentialsX's direct named-warp command. |

## Visual Ownership

MiraWarps owns the menu only. EssentialsX owns the teleport. MiraCosmetics owns successful teleport visuals.

No teleport particles are spawned merely because the GUI command was accepted, preventing false-positive or duplicate effects when Essentials denies, delays or later completes a teleport.

## Building

```bash
gradle clean build
```

The output JAR is created in `build/libs/`.


## Spawn

MiraWarps now owns the player-facing Spawn flow while EssentialsX remains the final teleport backend.

- `/spawn` requires `mirawarps.spawn`.
- `/setspawn` requires `mirawarps.setspawn`.
- Spawn has its own configurable warmup, cooldown and movement cancellation.
- Cooldowns are stored as absolute timestamps in `plugins/MiraWarps/spawn-data.yml` and survive restarts.
- `mirawarps.spawn.bypass.warmup` and `mirawarps.spawn.bypass.cooldown` bypass those controls.
- First-join routing is enabled by default.
- Respawn-at-spawn is configurable and disabled by default.
- Spawn can optionally appear as a dedicated entry in the Warps GUI.
- MiraCombatTag's global command lockdown blocks player `/spawn` while combat tagged before MiraWarps can begin the teleport.
- Successful teleport visuals remain owned by MiraCosmetics via the actual Bukkit teleport event.

The shared message prefix is `&5&lMira &8&l>> &r`.
