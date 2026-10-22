# Infinite Dungeon API

The shared library for the infinite-dungeon module suite. It ships no gameplay
of its own: installing it alone changes nothing you can see in game.

## What it provides

Other infinite-dungeon modules compile against this library instead of against
each other:

- **Dimension profiles** - a datapack registry (and a code registration API)
  that lets a pack or addon declare extra dungeon dimensions with their own
  layer limits, mob and boss pools, and curve overrides.
- **Ambient magicule service** - provider registration and querying for the
  magicule concentration at a position, plus concentration modifiers such as
  structure and portal elevation.
- **Entity power stat service** - a provider registry for a mob's AP, MP, and
  EP values, with a fallback metric when no provider is installed.
- **Lifecycle events** - layer, floor, gate, and boss events other modules
  observe to add announcements, advancements, or quest hooks.

## Dependencies

| | Mod | Why |
|---|---|---|
| Required | Minecraft | - |
| Required | NeoForge | - |
| Optional | none | Every provider is optional; absent providers fall back gracefully |

## Using this library

Modules in this suite consume it as a Maven dependency. The exact coordinates
and repository setup are recorded in `docs/DEVELOPMENT_PLAN.md`; a pack that
wants the full dungeon experience should install the core module alongside it.

## Credits

Bitwise Madness.

## License

GNU Lesser General Public License v2.1 - see `LICENSE`.
