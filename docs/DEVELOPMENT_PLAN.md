# infinite-dungeon-api Development Plan

This is the living source of truth for the library's design. It records what
the API exposes, why the suite is split the way it is, and which module owns
which mechanic.

## 1. Identity and conventions

- Repository: `HexaHexes/infinite-dungeon-api`.
- Mod id: `infinite_dungeon_api`. Package:
  `org.bitwisemadness.infinitedungeonapi`.
- Minecraft 1.21.1, NeoForge 21.1.259, JDK 21.
- Versioning: `1.0.0-alpha.N` per finished feature; clean `1.0.0` at release.
- Process: one changeset per PR, squash merge, version bump per finished
  feature, public API only, no mixins, ASCII-only sources, no generated
  textures.
- CI: junit, gametest, version-bump. The two Tiered Echoes rulesets are added
  after this repo's first CI PR merges.
- Publishing: the library publishes to the org's GitHub Packages Maven
  registry so other modules resolve it from Maven, never from local jars.

## 2. Role in the suite

The suite is split so no module depends on another by accident:

```
infinite-dungeon-tensura ------+
infinite-dungeon (core) -------+--> infinite-dungeon-api
infinite-dungeon-spawn-safety -+
other integrations ------------+
```

- **Dependency direction is inward only.** Modules depend on this library;
  this library depends on nothing but Minecraft and NeoForge.
- The library exists so two modules that need the same mechanic (the dungeon's
  magicule concentration, a mob's AP/MP/EP values, spawn classification) can
  share it without depending on each other.
- The spawn-safety module consumes this library alone: it works in a pack with
  no dungeon installed, and transparently sees the dungeon's values when the
  core module is present.

## 3. Public surface overview

| Package | Contents |
|---|---|
| `...api.profile` | Dimension profiles: schema, registry, resolution |
| `...api.magicule` | Ambient magicule service: providers, modifiers, queries |
| `...api.stats` | Entity power stat service: kinds, providers, fallback metric |
| `...api.event` | Lifecycle events (layer, floor, gate, boss, gate decisions) |
| `...api.data` | Datapack loading utilities shared by all modules |
| `...api.spawn` | Mob classification chain and spawn-type allowlist helpers |
| `...api.registry` | Generic registration helpers used by the above |

Everything in these packages is public API and covered by the stability
promise in section 10.

## 4. Dimension profiles

A profile tells the suite that a dimension is a dungeon dimension and what its
rules are. The core module ships one for its own dimension; addons and
datapacks add more.

- Datapack location:
  `data/<ns>/infinite_dungeon/dimension_profile/<name>.json`.
- Code registration: `DimensionProfiles.register(DimensionProfile)` for
  addons that prefer code.
- Fields:
  - `dimension`: dimension id this profile applies to (required).
  - `layerLimit`: maximum layer index (optional; absent means unbounded).
  - `floorsPerLayer`: `{ min, max }`, defaults from core config.
  - `pools`: `{ fodder, elite, boss }` pool ids (section 5 of the core plan
    documents the pool schema; references are namespaced).
  - `curves`: optional overrides for the D, M, and S coefficients.
  - `guardian`: `{ respawnCooldown, powerBandPercent }`.
  - `portal`: `{ returnMode, allowConstruction, reformMissingPortal }`.
  - `announcements`: entry and layer announcement text keys.
  - `spawnSafetyExempt`: default true (dungeon dimensions are exempt from the
    overworld safety gates).
- Resolution: a query maps the current dimension to its profile. No profile
  means "not a dungeon dimension" and the suite's systems disengage.
- Precedence: config file values are global defaults; a profile overrides
  them; an absent profile field falls back to the default.
- Reload: profiles are datapack data and reload with `/reload`.
- Validation: unknown pool ids or entity references are skipped with a log
  line, never a hard failure, so a profile can reference optional content.
- Requirement carried by every profile: world content must be seed-derived at
  chunk-generation time (the core plan's pregeneration rule). The API
  documents this requirement; it does not enforce it.

## 5. Ambient magicule service

The shared answer to "how much magicule is in this chunk".

- Unit convention: a plain `double` concentration. Tensura's own values are
  the reference scale; a scaling knob lives in the consumer (the spawn-safety
  gate's `attributeScale`), not here.
- Providers: `AmbientMagicule.registerProvider(provider)` where a provider
  answers `OptionalDouble at(Level, BlockPos)`. Providers are queried in
  registration order; the first non-empty answer wins.
- Fallback: no provider answers, or all return empty, yields 0.0 - the
  documented "cold" value that makes every consumer a no-op.
- Modifiers: `AmbientMagicule.registerModifier(...)` contributes elevation
  around placed content (dungeon structures, nether portals, tower entrances).
  Modifiers add to the resolved value; the modifier order is deterministic
  (registration order), and both additive and multiplicative forms are
  defined so pack authors can reason about stacking.
- Generation-time elevation registered by a module must itself be seed-derived
  or triggered by an explicit world event, so pregenerated chunks match
  organic ones.
- Threading and sides: queries are server-side and may occur during ticking;
  providers must not block and must be side-safe.

Spike-grounded notes (Tensura 2.0.1.3): `AreaMagiculeHelper.getMagicule(Level,
BlockPos)` is the natural provider implementation; `ChunkStorage` exposes
`setMagicule`, `setMaxMagicule`, and `addBlockModifier` for the write side, and
Tensura may recompute values during chunk initialize/reinitialize, so the
provider module owns the write timing.

## 6. Entity power stat service

The shared answer to "how strong is this mob", used by the boss schedule and
the spawn-safety gate.

- `StatKind`: `AP`, `MP`, `EP`. The API treats every kind as an opaque
  provider-supplied `double`; the meaning is the provider's business.
  - Tensura provider: AP = aura, MP = magicule, EP = the entity's stored
    energy, all read from Tensura's existence storage (spike finding).
- `EntityPowerStats.registerProvider(provider)` where a provider answers
  `OptionalDouble of(LivingEntity, StatKind)`.
- Fallback metric: when no provider answers, the API offers a documented
  attribute-based heuristic (health and attack damage derived) so boss
  ordering still functions in a pack without Tensura. Consumers choose whether
  to use the fallback or treat the value as absent.
- Provider-absent consumers treat the requirement as zero, which keeps the
  spawn gate a no-op without any stats integration.

## 7. Lifecycle events

Fired server-side by the core module and observed by integrations:

| Event | When | Consumer examples |
|---|---|---|
| `LayerEnteredEvent` | a player crosses into a new layer | announcements, advancements, quest hooks |
| `FloorEnteredEvent` | depth API reports a new floor within a layer | advancements, ambience |
| `LayerCompletedEvent` | a layer's guardian falls for the first time | advancements, guild ranks |
| `BossSlainEvent` | a layer boss dies (carries the boss and layer) | boss checklists, quests, loot hooks |
| `GateUnlockedEvent` | a gate transitions to open | portal/announcement integrations |
| `SpawnGateDecisionEvent` | the safety module is about to block a natural spawn | diagnostics, pack overrides |

Events are notifications unless documented cancellable; consumers must never
rely on firing order between modules.

## 8. Datapack loading utilities

Shared by every module that reads `data/<ns>/infinite_dungeon/**`:

- A namespaced loader that merges files of one data type across all datapacks,
  in deterministic order, reloadable with `/reload`.
- `required_mod` filtering: an entry naming an absent mod is skipped silently.
- Registry-id filtering: entries referencing absent registry entries are
  skipped, not fatal.
- Tag expansion: tag references expand through holder sets; empty tags are
  tolerated.
- Shared codecs so every module parses its data the same way.

## 9. Spawn classification and spawn-type helpers

- Classification chain (derived, never curated): datapack override, then the
  `NeutralMob` interface, then the `Monster` class or `MONSTER` category, then
  passive.
- Override data: `data/<ns>/infinite_dungeon/safety/classification.json`
  (override-only; no curated lists ship).
- API: `MobClassification.categoryOf(Entity)` returns `HOSTILE`, `NEUTRAL`, or
  `PASSIVE`.
- Spawn-type helpers: a documented natural-only allowlist helper
  (`MobSpawnType.NATURAL`) that consumers use to gate post-generation natural
  spawns; spawner, structure, command, egg, event, and patrol spawns are never
  in scope for gating.

## 10. Stability, publishing, and consumption

- Stability promise: everything under `...api.*` is public. Additive changes
  are minor; removals or signature changes are breaking and carry a `breaking`
  label so the version-bump workflow demands a major or minor bump.
- Publishing: a `publish` job (GitHub Actions, `packages: write`) publishes
  `org.bitwisemadness:infinite_dungeon_api` to
  `maven.pkg.github.com/HexaHexes/infinite-dungeon-api` on release tags.
- Consumption: module repos declare the GitHub Packages Maven repository and
  a pinned version. Reads on GitHub Packages require authentication; the
  consumer workflow supplies `permissions: packages: read` plus credentials.
  Local development likewise needs a read token; it is deferred until the
  first consumer needs it. If anonymous reads ever matter, the fallback is a
  public Maven host.
- Third-party mods continue to resolve through CurseMaven (or the official
  Modrinth maven); this policy only governs the suite's own modules.

## 11. Delivery waves

- Wave A1: scaffold (done) and the datapack loading utilities plus the
  profile schema and registry.
- Wave A2: ambient magicule service (providers and modifiers).
- Wave A3: entity power stat service with the fallback metric.
- Wave A4: lifecycle events.
- Wave A5: spawn classification and spawn-type helpers.
- Wave A6: publishing workflow and consumer documentation.

Each wave is one feature per PR and one alpha bump, verified by the three CI
checks before merge.

## 12. Test coverage plan

JUnit (pure logic, no Minecraft runtime where possible):

- profile JSON codec round-trip and precedence (default < profile);
- `required_mod` filtering, missing-registry-entry tolerance, deterministic
  merge order across packs;
- classification precedence with injectable predicates;
- modifier ordering and stacking math (additive and multiplicative);
- stat fallback heuristic and provider-absent behavior.

GameTest (live server):

- profile resolution for a registered dimension;
- provider registration and query with a stub provider;
- event firing on a simulated layer transition.

## 13. Decision record

- The suite splits into a core gameplay module, a standalone spawn-safety
  module, and per-integration modules; shared mechanics live here.
- Dependency direction is inward only; the library has no hard dependencies
  beyond Minecraft and NeoForge.
- The ambient magicule service answers per position with a plain double;
  absent providers yield 0.0 and make consumers no-ops.
- Entity power stats are provider-supplied opaque doubles with a documented
  fallback metric; Tensura maps AP to aura, MP to magicule, EP to stored
  energy.
- Spawn gating is natural-spawns-only by an explicit allowlist; the library
  provides the helper, the spawn-safety module applies it.
- Mob classification is derived (override, NeutralMob, Monster, passive).
- The library publishes to GitHub Packages; the suite's modules consume it
  from Maven and never from local jars.
