# MiraWarps

MiraWarps is the EssentialsX-backed warp GUI for the Mira Paper server suite. It presents the server's existing Essentials warps through a clean visual menu while leaving Essentials authoritative for warp data, teleport rules and permissions.

## Download

[**Download MiraWarps v0.1.0**](https://github.com/FiveSOCE/Mira-Warps/releases/download/v0.1.0/MiraWarps-0.1.0.jar)

## Requirements / Dependencies

- Paper 1.21.11
- Java 21
- EssentialsX 2.22.0 or newer

## How MiraWarps Works

Warp names are read live from EssentialsX, so MiraWarps never maintains a separate warp database. The GUI starts compact, expands as required and paginates safely for larger warp lists. Each warp is displayed as an Eye of Ender named after the Essentials warp, with unused menu space filled using the Mira grey-glass presentation.

Clicking a warp closes the GUI and dispatches Essentials' namespaced `/warp <name>` command. This means Essentials continues to enforce warp permissions, teleport behaviour and safety rules. After the player selects a warp, MiraWarps starts a configurable three-ring coloured particle effect around the player.

The `/warp` and `/warps` command bridge routes no-argument use into the GUI, while `/warp <name>` remains normal Essentials direct warp behaviour.

## Commands

| Command | Permission | What it does |
| --- | --- | --- |
| `/mwarps` | `mirawarps.use` | Opens the MiraWarps GUI. |
| `/mwarp` | `mirawarps.use` | Alias for `/mwarps`. |
| `/warp` | Essentials access + MiraWarps routing | Opens the MiraWarps GUI when used without a warp name. |
| `/warps` | Essentials access + MiraWarps routing | Opens the MiraWarps GUI. |
| `/warp <name>` | Essentials permissions | Remains EssentialsX's direct named-warp teleport command. |

## Permissions

| Permission | Default | What it does |
| --- | --- | --- |
| `mirawarps.use` | Everyone | Allows access to the MiraWarps GUI through the MiraWarps command surface. |

EssentialsX permissions continue to control access to the underlying warps themselves.
