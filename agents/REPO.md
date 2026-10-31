# Repo rules — Infinite Dungeon API

Repo-specific rules and notes. The org-wide `AGENTS.md` beside this file is
maintained in [`neoforge-mod-template`](https://github.com/HexaHexes/neoforge-mod-template)
and pulled in verbatim - **never hand-edit it.** Everything in this file is this
repo's own and is edited freely; keep it aligned with the org rules rather than
restating them.

**Repo:** `HexaHexes/infinite-dungeon-api` · **Mod ID:** `infinite_dungeon_api` · **Branch:** `main`
**Platform:** Minecraft 1.21.1 / NeoForge 21.1.259 / JDK 21 · **Jar:** `build/libs/infinite_dungeon_api-<version>.jar`

The public API and shared mechanics library of the infinite-dungeon module
suite. It contains no gameplay: it defines the service registries (dimension
profiles, ambient magicule, entity power stats) and lifecycle events that the
core module and every integration compile against. It must stay installable on
its own with no hard dependencies beyond Minecraft and NeoForge.

**Dependencies**
- Required: Minecraft, NeoForge.
- Optional: none hard-coded. Every provider (Tensura stats, magicule sources,
  party resolvers) registers from another module; absent providers fall back to
  documented defaults.

**Test:** `./gradlew runGameTestServer` - `ApiSmokeGameTests` (10 tests).
`expectedTests = 10` in `build.gradle` must match the `@GameTest` count.

**Remote:** `github.com/HexaHexes/infinite-dungeon-api` (public, default branch
`main`).

---

## Gotchas (do not regress them)

- This library must never depend on another infinite-dungeon module. Dependencies
  point inward only: modules depend on the API, never the reverse.
- Public API stability is the whole product. Prefer additive changes; announce
  breaking ones with a major/minor bump and a `breaking` label.
- The `empty` GameTest structure lives at
  `src/gametest/resources/data/infinite_dungeon_api/structure/empty.nbt`; moving
  it or renaming the mod id breaks the smoke test.

## Naming - this repo

- Repo and folder: `infinite-dungeon-api`; `mod_id`: `infinite_dungeon_api`;
  Java base package `org.bitwisemadness.infinitedungeonapi`, with the Maven
  `group` in `build.gradle` matching.

## CI & release model - this repo

- `ci.yml` runs the template's three checks (junit, gametest, version-bump).
- `release.yml` cuts the GitHub release for `v<mod_version>` on a push to `main`
  (pre-releases only when the repo variable `ALPHA_RELEASES` is true) - until
  now releases here were cut by hand.
- This is the suite's published library: `publish.yml` uploads the artifact to
  the org's GitHub Packages registry when that release is published, under the
  version in `gradle.properties`.
