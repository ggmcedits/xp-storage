# Experience Storage – Forge 1.20.1 port

Port of the **Experience Storage** Bedrock addon (by Effect99, namespace `effectoo`) to Forge 1.20.1 (47.x).
Mod id: `experience_storage`, package `com.experiencestorage`.

> Check the original addon's license allows porting/redistribution before publishing this.
> `mod_license` in `gradle.properties` is a placeholder ("All Rights Reserved").

## Build

Requires JDK 17.

1. Add the Gradle wrapper (not included, it is a binary): either copy `gradlew`, `gradlew.bat` and
   `gradle/wrapper/gradle-wrapper.jar` from the official Forge 1.20.1 MDK, or run `gradle wrapper --gradle-version 8.1.1`.
2. `./gradlew build`  -> jar in `build/libs/`
3. `./gradlew runClient` to test.

## Content

| Bedrock | Forge |
|---|---|
| `effectoo:xp_tank` | `experience_storage:xp_tank` (block entity, 2920 XP = level 40, 14 fill stages, glows when non-empty) |
| `effectoo:xp_pipe` | `experience_storage:xp_pipe` (auto-connecting, hollow glass tube) |
| `effectoo:xp_extractor` | `experience_storage:xp_extractor` |
| `effectoo:xp_valve` | `experience_storage:xp_valve` (place against a pipe face, right-click to open/close) |
| `effectoo:orb_fairy` | `experience_storage:orb_fairy` + spawn egg |
| `effectoo:xp_fluid` | `experience_storage:xp_fluid` (internal, the orb travelling through pipes) |
| `effectoo:xp_storage` entity | removed: replaced by the tank's block entity |

Creative tab: **ExperienceFlow**. Languages: en_us, it_it. Recipes unchanged.

## Behaviour (same as the addon unless noted)

* Sneak on a tank: your XP is poured in. Sneak on an extractor: it goes into the pipe below.
* Right-click a tank with a glass bottle: get an XP bottle (needs > 10 XP stored).
* A tank with a **non-blocked pipe directly below** pushes XP out into the pipe network (redstone signal pauses it).
  A tank directly below another tank receives from it.
* Any pipe touching a tank on a side or bottom face empties into it. Closed valve = pipe is blocked.
* Extractor: mobs that die on it with no player credit give 1-11 XP (not babies, golems, players, /kill, cactus, berries);
  XP orbs touching it are absorbed. Needs an open pipe below; otherwise normal orbs drop as usual.
* Orb fairy: spawns in the dark (overworld), vanishes in light, invulnerable, takes XP from nearby players and
  runs from them, spills XP when hit, vanishes if hit empty in melee.

## Intentional differences

* Extractor takes the mob's **real** XP reward on player kills (the addon could only give random 1-11) and
  absorbs merged orb stacks at full value.
* Removing a closed valve re-opens its pipe (the addon left the pipe stuck closed).
* Pipes move XP with a real pathing entity instead of tag/impulse hacks; if a pipe is removed the XP drops as orbs.
* Breaking a tank drops its XP as vanilla orbs. Empty-hand right-click on a tank shows its contents.
* Fairy natural spawn weight is 10 (`data/.../forge/biome_modifier/orb_fairy_spawns.json`); the fairy uses
  the AMBIENT mob category. Named fairies do not vanish in light.
* Not ported: `xp_liquid_green.png` (unused by the addon), the green pulsing overlay on tank liquid,
  custom `orb_despawn` particle (uses a firework spark), `scriptevent`/dynamic-property plumbing.

## Models

Generated from the Bedrock `.geo.json` files. Tank, pipe, extractor and valve-open should match the original;
the closed-valve handle (rotated bone) and its UV orientation are the likeliest thing to need a touch-up in Blockbench.
