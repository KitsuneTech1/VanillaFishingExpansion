<p align="center"><a href="https://kitsunetechnologies.org/work"><img src="https://raw.githubusercontent.com/KitsuneTech1/.github/main/assets/kitsune-banner.svg" alt="Built by Kitsune Technologies" width="760"></a></p>

# Angler's Almanac

Paper plugin with vanilla-friendly fishing overhaul, hidden leveling, and improved loot diversity.

(VanillaFishingExpansion)

[![Build Status](https://github.com/KitsuneTech1/VanillaFishingExpansion/actions/workflows/ci.yml/badge.svg)](https://github.com/KitsuneTech1/VanillaFishingExpansion/actions)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
![Paper](https://img.shields.io/badge/paper-26.1.2-brightgreen.svg)
![Java](https://img.shields.io/badge/java-25-orange.svg)

## Overview

Adds a silent fishing progression system on top of normal Minecraft fishing. Players gain hidden levels (1-100) that unlock rarer loot, chase streaks and journal completion, and participate in seasonal events like Blood Moon fishing. All features are vanilla-friendly - no resource packs or new items except for craftable shard rewards.

## Features

- Hidden fishing levels (up to 100) that scale drop chances the more a player fishes
- Biome-aware loot table with common, uncommon, rare, and ultra-rare tiers
- Fishing streak system with a "Fishing Frenzy" bonus at 10 catches in a row
- Fish journal that tracks collection progress per player
- Leaderboard (`/fishtop`) with optional broadcast when a new #1 is set
- Seasonal events: rain bonus, double XP days, and Blood Moon loot (wither skulls, ghast tears, nether stars)
- Angler's Rod: a catchable custom rod with a small catch-rate bonus
- Nether lava fishing, unlocked at a configurable level
- Trident and Totem of Undying craftable from shard drops


## Architecture & Mechanics

```mermaid
flowchart TD
    A[Player Casts Fishing Rod] --> B[PlayerFishEvent Intercepted]
    B --> C[Evaluate Catch Context: Biome, Weather, Blood Moon]
    C --> D[Retrieve Player Hidden Fishing Level: 1-100]
    D --> E{Check Streak & Frenzy}
    E -->|10+ Streak| F[Fishing Frenzy Multipliers Active]
    E -->|Normal| G[Standard Roll]
    F & G --> H[Roll Tier: Common, Uncommon, Rare, Ultra-Rare, Shards]
    H --> I[Update Fish Journal & Leaderboard Stats]
    I --> J[Award Catch & Display Silent XP Progression]
```

## Commands

| Command | Description | Usage |
|---|---|---|
| `/fishingstats [player]` | View hidden fishing statistics (admin only) | `/fishingstats [player]` |
| `/fishtop` | View the fishing leaderboard | `/fishtop` |
| `/fishjournal` | View your fish collection progress | `/fishjournal` |

## Permissions

| Permission | Description | Default |
|---|---|---|
| `vanillafishing.stats` | Allows viewing fishing statistics | op |

## Config

Key options in `config.yml`:

- `leveling.base-fish-required`, `leveling.scaling-multiplier`, `leveling.max-level`: control how fast players level up
- `drops.*-base`: base drop chance percentages for each rarity tier, scaled by level
- `features.*`: toggle streaks, the Angler's Rod, fish journal, leaderboard, and nether fishing independently
- `streak.timeout-seconds`, `streak.frenzy-threshold`, `streak.frenzy-xp-multiplier`, `streak.frenzy-loot-bonus`: tune the streak/frenzy system
- `events.rain-bonus`, `events.double-xp-days`, `events.blood-moon`: seasonal loot and XP bonuses
- `leaderboard.top-count`, `leaderboard.broadcast-new-leader`: leaderboard display settings
- `items.anglers-rod.catch-bonus`, `items.anglers-rod.drop-chance`: Angler's Rod tuning
- `logging.frenzy-activations`: server console logging for frenzy activations

## Quick Start

### What you need

- A Paper server matching api-version 26.1.2 or newer. Download it from https://papermc.io/downloads
- Java 25 or newer. Download it from https://adoptium.net. Check what you have with `java -version` in a terminal.
- This plugin will not work on vanilla Minecraft or on Realms. You need a Spigot/Paper server you host yourself.

### Step by step

**Option A: download the jar**

Go to the [latest release](https://github.com/KitsuneTech1/VanillaFishingExpansion/releases/latest) and download the `.jar` file attached to it. No build tools needed.

**Option B: build from source**

1. Install a JDK 25+ from https://adoptium.net if you don't have one.
2. Clone the repo (`git clone https://github.com/KitsuneTech1/VanillaFishingExpansion.git`) or click "Code > Download ZIP" on GitHub and unzip it.
3. From the project folder, run `mvn clean package`.
4. The built jar will be in `target/VanillaFishingExpansion-2.0.0.jar`.

Then, for both options:

- Drop the jar into your server's `plugins` folder.
- Restart the server completely (not `/reload`, a full restart).
- Check that it loaded by running `/fishtop` in game or console. You should see a leaderboard message, not an "unknown command" error.

**It worked if** `/fishtop` responds with a leaderboard (even an empty one) and the console log shows VanillaFishingExpansion enabling with no errors during startup.

**Troubleshooting:**

- Console says "Unsupported API version" or the plugin won't load: your server is older than Paper 26.1.2. Update your server first.
- Command shows "Unknown command": the plugin didn't load. Check `plugins/` for the jar and look at the startup log for errors, and confirm you're running Java 25+.
- "You do not have permission" on `/fishingstats`: that command defaults to op only. Grant `vanillafishing.stats` to the player or run it as an op.

## Build

```
mvn clean package
```

The built jar is output to `target/VanillaFishingExpansion-<version>.jar`.

## License

MIT. Copyright (c) 2026 Kitsune Technologies LLC. Do what you like with it, commercially or otherwise, just keep the copyright notice. Full text in [LICENSE](LICENSE).

---

<p align="center"><sub>Part of <b>Buildtober 2026</b>: 31 open source projects in 31 days. Built by <a href="https://kitsunetechnologies.org/work">Kitsune Technologies</a>. More at <a href="https://github.com/KitsuneTech1">github.com/KitsuneTech1</a>.</sub></p>
