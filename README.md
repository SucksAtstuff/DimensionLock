# DimensionLock

A server-side NeoForge mod for Minecraft 1.21.1 that lets you lock any dimension (vanilla or modded) at runtime with a command. No config files or restarts needed.

When the server starts, DimensionLock picks up every dimension that's loaded, including those added by other mods in your modpack. Ops can then lock or unlock them individually in-game.

## Commands

All commands require permission level 2 (op).

| Command | Description |
| --- | --- |
| `/dimlock list` | Lists every loaded dimension and whether it's `[open]` or `[LOCKED]` |
| `/dimlock lock <dimension>` | Locks a dimension. Players currently inside are sent to world spawn |
| `/dimlock unlock <dimension>` | Unlocks a dimension |

Dimension names tab-complete, so modded dimensions show up automatically.

**Examples**

```
/dimlock lock minecraft:the_nether
/dimlock lock minecraft:the_end
/dimlock unlock minecraft:the_nether
```

## What gets blocked

When a dimension is locked, nothing can travel into it:

- Nether and End portals (the Nether portal won't generate an exit portal in a locked Nether either)
- `/tp`, `/execute in ...` and other commands
- Modded portals and teleporters that use the standard dimension-change logic

Players who log in while standing in a locked dimension, or who somehow end up in one, are sent to world spawn.

The overworld can't be locked, since it's where players get sent when they're removed from a locked dimension.

## Persistence

Locked dimensions are saved per world in `<world>/data/dimensionlock.dat`, so they stay locked across server restarts.

## Installation

Drop the jar into the server's `mods` folder. The mod is only needed on the server; players can join without installing it.

## Building

```
./gradlew build
```

The jar ends up in `build/libs/`.
