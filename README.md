# MiraWarps

EssentialsX-backed warp and spawn presentation layer for the Mira Paper server suite.

MiraWarps provides a clean paginated warp GUI and a Mira-owned `/spawn` warmup/cooldown flow while leaving EssentialsX authoritative for warp data and EssentialsSpawn authoritative for the final spawn location.

## Version-specific downloads

| Paper version | Java | JAR name | Download |
| --- | --- | --- | --- |
| **1.18.2** | 17 | `MiraWarps_1.18.2.jar` | [Legacy release](https://github.com/FiveSOCE/Mira-Warps/releases/tag/dev-1.18.2) |
| **1.21.11–26.2** | Java 21 baseline; modern Paper may require Java 25 | `MiraWarps_1.21.11-26.2.jar` | [Modern release](https://github.com/FiveSOCE/Mira-Warps/releases/tag/compat-modern-1.21.11-26.2) |

The direct downloads become available after successful CI and publication. Full runtime validation and Paper 1.19–1.21.10 compatibility remain unverified.

[Legacy source](https://github.com/FiveSOCE/Mira-Warps/tree/compat/1.18.2) · [Modern source](https://github.com/FiveSOCE/Mira-Warps) · [All releases](https://github.com/FiveSOCE/Mira-Warps/releases)

## Dependencies and compatibility

- **Required:** Matching Paper and Java runtime, and an **EssentialsX** version compatible with your Paper server. The plugin descriptor has the hard dependency `Essentials`.
- **Optional:** EssentialsSpawn (required specifically for `/spawn` and `/setspawn` functionality), MiraCosmetics for teleport visuals, MiraCombatTag for teleport command restrictions.
- **MiraCore:** Not required.
- EssentialsX retains warp locations and named-warp handling. MiraWarps provides the warp selection GUI and its own spawn warmup/cooldown without a duplicate Essentials teleport timer.
- On Paper 1.18.2 use a compatible EssentialsX/EssentialsSpawn pair; newer EssentialsX JARs may not support Java 17.
- Back up `plugins/MiraWarps/` and the Essentials warp/spawn data before switching builds.

## Commands and permission nodes

| Command | Permission | Purpose |
| --- | --- | --- |
| `/mwarps`, `/mwarp` | `mirawarps.use` (everyone) | Open warp GUI |
| `/warp`, `/warps` | Essentials warp access | Open list or use an Essentials warp |
| `/warp <name>` | Essentials warp permission | Direct named warp |
| `/spawn` | `mirawarps.spawn` (everyone) | Teleport to spawn with one warmup |
| `/setspawn` | `mirawarps.setspawn` (OP) | Set EssentialsSpawn destination |

Bypass permissions: `mirawarps.spawn.bypass.warmup` and `mirawarps.spawn.bypass.cooldown` (OP).

## Warp GUI

MiraWarps reads warp names live from EssentialsX and does not maintain a second warp database.

- `/mwarps` opens the GUI
- `/warp` with no warp name routes into the GUI
- `/warps` routes into the GUI
- `/warp <name>` remains direct EssentialsX named-warp behavior
- the GUI expands/paginates as required

Selecting a warp dispatches the Essentials namespaced warp command. Essentials continues to own named-warp permissions, delays, cooldowns and the final teleport.

## Spawn Flow

MiraWarps owns the player-facing `/spawn` flow while EssentialsSpawn remains the final spawn/setspawn backend.

Spawn supports:

- configurable warmup
- configurable cooldown
- movement cancellation
- restart-safe absolute cooldown timestamps
- first-join routing
- optional respawn-at-spawn
- optional Spawn entry in the Warps GUI
- warmup/cooldown bypass permissions

MiraCombatTag's global command lockdown can block `/spawn` before the teleport begins.

## Single Warmup Ownership — v0.1.8

MiraWarps now owns the **single** configured `/spawn` warmup.

After MiraWarps finishes its own warmup and cooldown checks, it teleports directly to the EssentialsSpawn location instead of starting a second Essentials teleport timer. This removes the previous double-warmup behavior.

The final destination remains the configured EssentialsX Spawn location; MiraWarps simply prevents two independent warmup systems from competing.

## MiraCosmetics Integration

MiraWarps does not render its own teleport particles.

When MiraCosmetics is installed:

- teleport warmup presentation begins with the real MiraWarps spawn queue
- cancellation presentation fires if movement cancels the queue
- completion presentation fires only after the actual successful Bukkit/Paper teleport
- player Visual and Audio toggles are respected independently

This keeps MiraWarps responsible for teleport state while MiraCosmetics owns presentation.

## Commands

| Command | Permission | Purpose |
| --- | --- | --- |
| `/mwarps` | `mirawarps.use` | Opens the warp GUI. |
| `/mwarp` | `mirawarps.use` | Alias for `/mwarps`. |
| `/warp` | normal warp access | Opens the GUI when no warp name is provided. |
| `/warps` | normal warp access | Opens the GUI. |
| `/warp <name>` | Essentials permissions | Direct Essentials named warp. |
| `/spawn` | `mirawarps.spawn` | Runs the MiraWarps spawn flow. |
| `/setspawn` | `mirawarps.setspawn` | Uses the EssentialsSpawn backend to set spawn. |

## Spawn Permissions

```text
mirawarps.spawn
mirawarps.setspawn
mirawarps.spawn.bypass.warmup
mirawarps.spawn.bypass.cooldown
```

## Persistence

Spawn cooldown timestamps are stored in:

```text
plugins/MiraWarps/spawn-data.yml
```

## Ownership Model

- **EssentialsX** owns warp definitions and named-warp behavior.
- **EssentialsSpawn** owns the configured spawn location.
- **MiraWarps** owns the GUI and player-facing spawn queue/warmup/cooldown.
- **MiraCosmetics** owns teleport visual/audio presentation.

Keeping these responsibilities separate prevents duplicate timers and duplicate particle/sound effects.

## Building

```bash
gradle clean build
```

The output JAR is created in `build/libs/`.
