# Changelog

## 1.0.0 - 2026-11-05

Initial release for Minecraft 1.21.1 (NeoForge).

### Added
- `/dimlock list` shows every loaded dimension, vanilla and modded, and whether it's open or locked
- `/dimlock lock <dimension>` locks a dimension and sends any players inside to world spawn
- `/dimlock unlock <dimension>` unlocks a dimension
- Travel into a locked dimension is blocked for Nether and End portals, `/tp`, `/execute in`, and modded portals and teleporters that use the standard dimension-change logic
- Nether portals no longer generate an exit portal inside a locked dimension
- Players who log in or end up inside a locked dimension are sent to world spawn
- Locked dimensions are saved per world and stay locked across restarts
- All loaded dimensions are listed in the server log on startup
- Server-side only: players can join without installing the mod
