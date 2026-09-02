# MiraWarps

EssentialsX-backed warp GUI for the Mira Paper ecosystem.

## Download

**Current release: MiraWarps v0.1.0**

- [Download MiraWarps-0.1.0.jar](https://github.com/FiveSOCE/Mira-Warps/releases/download/v0.1.0/MiraWarps-0.1.0.jar)
- [View all releases](https://github.com/FiveSOCE/Mira-Warps/releases)

## Behaviour

- `/warp` and `/warps` with no arguments open the MiraWarps GUI.
- `/mwarps` and `/mwarp` also open the GUI directly.
- Warp names are read live from EssentialsX. MiraWarps does not maintain a second warp database.
- Each warp is represented by an Eye of Ender named after the Essentials warp.
- Dead GUI space is filled with blank grey stained glass panes using the enchantment-glint visual effect.
- Clicking a warp dispatches the namespaced Essentials command, so Essentials remains authoritative for warp permissions, teleport rules and safety checks.
- The GUI grows from 9x3 upward and paginates safely when there are more than 28 warps.

## Particle effect

After a warp is selected, MiraWarps creates three rotating particle rings around the player:

- Blue at head height
- Red at mid-body height
- Green just above the feet

The effect uses coloured `DUST` particles and is configurable in `plugins/MiraWarps/config.yml`.

## Requirements

- Paper 1.21.11
- Java 21
- EssentialsX 2.22.0+

## Building

```bash
gradle clean build
```

Output:

```text
build/libs/MiraWarps-0.1.0.jar
```
