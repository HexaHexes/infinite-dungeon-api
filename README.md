# Infinite Dungeon API

The shared library for the infinite-dungeon module suite. The gameplay lives
in the other modules; this one holds what they share.

## What it provides

Other infinite-dungeon modules compile against this library instead of against
each other:

- **Dimension profiles**: a datapack registry (and a code registration API)
  that lets a pack or addon declare extra dungeon dimensions with their own
  layer limits, mob and boss pools, and curve overrides.
- **Ambient magicule service**: provider registration and querying for the
  magicule concentration at a position, plus concentration modifiers such as
  structure and portal elevation.
- **Entity power stat service**: a provider registry for a mob's AP, MP, and
  EP values, with a fallback metric when no provider is installed.
- **Lifecycle events**: layer, floor, gate, and boss events other modules
  observe to add announcements, advancements, or quest hooks.

## Dependencies

| | Mod | Why |
|---|---|---|
| Required | Minecraft | - |
| Required | NeoForge | - |
| Optional | Open Parties and Claims | Party tier in the party resolver (api#65) |
| Optional | FTB Teams | Party tier in the party resolver (api#65) |
| Optional | Tensura: Reincarnated | Chunk magicule bridge (api#68) |

Every integration above is compile-only dev wiring behind a ModList gate:
this library ships and runs without any of them.

## Using this library

Modules in this suite consume it as a Maven dependency from the org's GitHub
Packages registry:

```groovy
repositories {
    maven {
        url = uri('https://maven.pkg.github.com/HexaHexes/infinite-dungeon-api')
        credentials {
            username = System.getenv('GITHUB_ACTOR')
            password = System.getenv('GITHUB_TOKEN') // read:packages
        }
    }
}

dependencies {
    compileOnly 'org.bitwisemadness:infinite_dungeon_api:<version>'
}
```

Reads on GitHub Packages always require authentication. In CI the token needs
`packages: read`; the workflow token of one repository cannot read a package
owned by another, so a read token is supplied as a secret. A pack that wants
the full dungeon experience should install the core module alongside it.

Publication happens when a GitHub release is published: the `Publish` workflow
builds and uploads the artifact under the version in `gradle.properties`.

## Credits

Bitwise Madness.

## License

GNU Lesser General Public License v2.1 - see `LICENSE`.

## AI disclosure

Parts of this project were written with the assistance of AI coding tools.
AI-assisted code, data and documentation ship only after human review by the
maintainers.
