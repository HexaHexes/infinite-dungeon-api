# Agent rules and conventions - Infinite Dungeon API

**Repo:** `HexaHexes/infinite-dungeon-api` - **Mod ID:** `infinite_dungeon_api` - **Branch:** `main`
**Platform:** Minecraft 1.21.1 / NeoForge 21.1.259 / JDK 21 - **Jar:** `build/libs/infinite_dungeon_api-<version>.jar`

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

**Test:** `./gradlew runGameTestServer` - `ApiSmokeGameTests` (1 test).
`expectedTests = 1` in `build.gradle` must match the `@GameTest` count.

**Gotchas**
- This library must never depend on another infinite-dungeon module. Dependencies
  point inward only: modules depend on the API, never the reverse.
- Public API stability is the whole product. Prefer additive changes; announce
  breaking ones with a major/minor bump and a `breaking` label.
- The `empty` GameTest structure lives at
  `src/gametest/resources/data/infinite_dungeon_api/structure/empty.nbt`; moving
  it or renaming the mod id breaks the smoke test.

---

## Branch sync (do this first)

Before starting any new development in this repo:

1. `git status` - confirm what uncommitted work exists. Never disturb or sweep it.
2. `git pull --ff-only` - sync with the remote. A fast-forward is the normal case,
   because we push straight to the shared branch.
3. If the pull would **not** fast-forward (the branch has diverged), or if it would
   collide with local uncommitted work, **stop and ask** rather than merging, rebasing
   or stashing on your own.

## Scope doctrine

- **One mod + bundled datapack.** No KubeJS scripts.
- **Data-driven over code** - prefer JSON/datapack content to Java when both work.
- **Public API only, no mixins** in this repository.

## Verification

- Verify with the in-repo GameTest run (`./gradlew runGameTestServer`).
  Never verify against a live CurseForge instance.
- Keep `expectedTests` in `build.gradle` in sync with the `@GameTest` methods.
- **Client-render features cannot be proven headless.** The honest scope is
  "build green + the user's in-game check" - say so rather than overclaiming.

## Build environment

- JDK 21 (`JAVA_HOME` -> JDK 21); the toolchain targets Java 21.
- The Gradle wrapper is committed. `./gradlew build` compiles,
  `./gradlew runGameTestServer` runs the GameTest server.
- **Dependencies resolve through Maven, never local folders.** Third-party mods use
  CurseMaven coordinates (`curse.maven:<slug>-<projectId>:<fileId>`, repository
  `https://cursemaven.com`); Modrinth-exclusive mods use the official Modrinth maven
  (`maven.modrinth:<project-slug>:<version>`, repository
  `https://api.modrinth.com/maven`). Modules of this suite consume each other from
  the org's GitHub Packages Maven registry - never from local jars. Gitignored
  `libs/` and `testmods/` staging folders are retired: CI must resolve every
  dependency from a clean checkout. Pin the exact version the repo was verified
  against; bump pins deliberately, never implicitly.
- On `run/` file-lock errors, kill any stale `java` process and retry.

## PowerShell quirks (this org is developed on Windows)

- A `git push` "error" in PowerShell is usually stderr passthrough - success is the
  `hash..hash  branch -> branch` line, not the absence of red text.
- `New-Object Type(args)` breaks on arithmetic - use `::new()` or a parenthesized
  argument.
- **Never write files with `Set-Content -Encoding UTF8`** - PowerShell 5.1 writes a
  UTF-8 BOM that breaks Gradle's parser and YAML. Use
  `[System.IO.File]::WriteAllText($path, $text)` (no BOM). Re-check for BOMs after
  any script-written edit.

## Git & release

- Remote is SSH: `git@github.com:HexaHexes/infinite-dungeon-api.git`.
- Push only after tests are green **and** the user has given the go.
- Never sweep the user's uncommitted work into a commit - commit only what is intended.
- Commit subjects: concise, imperative.
- **Version bump on finish:** always bump the version when a new feature or bugfix
  is finished.
- **One feature or bugfix per branch:** develop each on its own feature branch and
  merge it into main/master through a pull request with a **squash merge**. The PR
  body must detail the work done, following `.github/pull_request_template.md`.

## House style

- **No icons** unless the user explicitly asks.
- READMEs are **player-facing only**: pitch, mechanics in plain terms, in-game usage,
  required/optional dependencies, credits, license. No build internals, no class names.
- This repo is **LGPL v2.1**, copyright Bitwise Madness. `NOTICE` / attribution files
  are mandatory and must never be deleted.
- Deploy to the Tensura RPG instance **only** when the user asks.

## Naming

- **Repo and folder:** lowercase, hyphenated (e.g. `infinite-dungeon-api`).
- **`mod_id`:** lowercase with underscores (e.g. `infinite_dungeon_api`). Minecraft
  requires `[a-z][a-z0-9_]{1,63}`.
- **Jar / `archivesName`:** follows `mod_id` by default.
- **Java base package:** `org.bitwisemadness.<project-name>` - repo name lowercased,
  separators stripped (e.g. `ds-tensura-fix` -> `org.bitwisemadness.dstensurafix`).
  The Maven `group` in `build.gradle` matches (`org.bitwisemadness`).
- Keep repo name, folder name, `mod_id`, and the Java package aligned; the rename
  checklist in `README.md` walks the whole set.
- **This template is canonical for org-wide rules.** When a rule is added or changed
  in any repo's `agents/AGENTS.md`, mirror the same change into the template so new
  repos inherit it.

## CI & release model

- `ci.yml` runs three checks that back the branch protection rulesets: **junit**
  (build + the JUnit suite, asserted non-vacuous), **gametest** (`runGameTestServer`,
  graded on the `All N required tests passed` log line - the exit code is not a
  reliable signal) and **version-bump** (a beta or stable bump must change the
  version file and nothing else; an `-alpha` bump may bundle work; a PR labelled
  `breaking` forces the next clean bump to raise the major or minor version).
- **Branch protection:** the default branch is guarded by two repository rulesets -
  `protect-main` (PR-only, squash merge only, strict `junit`/`gametest`/`version-bump`
  status checks, no force-pushes, no branch deletion) and
  `require-review-from-write-access` (one approving review; the listed org owners
  bypass). Direct pushes to the default branch are rejected by GitHub.
- `version-bump.yml` opens an isolated version-bump PR (only the version file changes).
- CurseForge publishing (`curseforge.yml`) is **deferred** until the mod has a CurseForge
  listing and the `CURSEFORGE_PROJECT_ID` / `CURSEFORGE_API_KEY` secrets. Do not add it
  speculatively.
