# OreBonus — Configurable Ore Processing Rewards

**NeoForge 1.21.1 | Ore Processing | Configurable Chances, Time & Energy | Expert Mode**

Reworked ore-processing recipes for Create Crushing Wheels and Immersive Engineering's Crusher &
Arc Furnace, with **every probability reward, processing time and energy cost tunable through a
single config file** — from casual to expert, one TOML to rule them all.

---

## ✨ Features

### 🔩 Complete ore processing chains (Iron / Copper / Gold / Zinc / Silver / Aluminum / Lead / Nickel / Uranium)

- **Create Crushing** — shallow ore: main output + 10% bonus + 75% Experience Nugget (+ cobblestone);
  deepslate ore: main output + 50% bonus (+ cobbled deepslate); copper 4/6 main outputs, gold 2 nuggets.
  Base processing time defaults to vanilla Create values (250/350/400 ticks).
- **IE Crusher** — crushed ore: stable 2 dusts; ore: 1 dust + 10% / 50% bonus secondary.
  Energy defaults to vanilla IE (6000 RF).
- **IE Arc Furnace** — shallow/deepslate ore: 1 ingot + slag.
  Time & energy default to vanilla IE (200 ticks / 102400 RF).
- **Zinc line** — new Zinc Dust item: crush crushed zinc for 2 dusts, smelt/blast it into ingots,
  and use it as the brass-alloy additive.
- **Dust alloys** (Arc Furnace, stacked single-slot output) — copper dust + 4 dusts → **1 slot × 4 Brass Ingots** + slag;
  cobblestone + 2 dusts → **1 slot × 2 Andesite Alloy** + slag. **Product is fixed; main input, additives,
  output count, slag, time & energy are all user-configurable** (see below).

### 🎚 Fully configurable chances, time & energy (`config/orebonus-common.toml`)

11 knobs per ore: shallow/deepslate bonus chance, nugget chance & count, shallow/deepslate main
output count, Create crushing time (shallow/deepslate), IE crusher energy, IE arc furnace time &
energy. Chances default to 10%/50%/75%; **time & energy default to the
vanilla mod values**; plus a **global chance multiplier** for one-click difficulty scaling.
Apply with in-game `/reload` — no restart needed.

```toml
[general]
bonusChanceMultiplier = 1.0    # 0.5 = halved rewards (harder), 2.0 = doubled (easier)
disableHammerCrushing = false  # expert-mode switch (below)

[ores.iron]
shallowBonusChance = 0.10
deepslateBonusChance = 0.50
expNuggetChance = 0.75
expNuggetCount = 1
shallowMainCount = 1
deepslateMainCount = 1
createCrushingTimeShallow = 250    # Create crushing base time in ticks (IE ores: 400)
createCrushingTimeDeepslate = 350  # Create crushing base time in ticks (IE ores: 400)
ieCrusherEnergy = 6000             # IE crusher RF per operation (ore/deepslate/crushed share it)
ieArcTime = 200                    # IE arc furnace time in ticks
ieArcEnergy = 102400               # IE arc furnace RF
```

#### Fully customizable dust alloys

The two alloy recipes keep only two things fixed: **arc furnace only, and the product itself**.
Main input, up to 4 additives, output count, slag on/off, time & energy are all editable —
or disable the recipe entirely with `enabled=false`:

```toml
[alloys.brass]                      # product fixed: Brass Ingot
enabled = true
time = 200
energy = 102400
inputItem = "immersiveengineering:dust_copper"
additive1 = "orebonus:zinc_dust"
additive2 = "immersiveengineering:dust_steel"
additive3 = "immersiveengineering:dust_aluminum"
additive4 = "immersiveengineering:dust_nickel"
outputCount = 4
produceSlag = true
```

### 🏭 Expert mode (deep Create integration)

Set `disableHammerCrushing = true` to remove every IE crafting-table recipe of
"ore / raw ore + Engineer's Hammer → dust". All dusts must then come from the IE Crusher or
Create Crushing Wheel — perfect for expert-style modpacks. Off by default.

---

## 📦 Dependencies

- **NeoForge 21.1+** (Minecraft 1.21.1)
- **Create 6.0.10+** (optional, but the mod is built around it)
- **Immersive Engineering 12.4.2+** (optional, same)

## ❓ FAQ

- **Where is the Zinc Dust?** Creative inventory → Ingredients tab; search "zinc dust" in JEI.
- **Config changes don't apply?** Run `/reload` in-game.
- **Want vanilla-like chances back?** Set each ore's `shallowBonusChance` / `deepslateBonusChance`
  to 0.75 and keep `expNuggetChance` at 0.75 (time & energy already default to vanilla).

## 📜 License

Author: **lodest-game**. MIT, 100% original code (no third-party mod code copied). Source, build guide
and full config documentation: [GitHub (lodest-game)](https://github.com/lodest-game).

## 🗺 Roadmap

- [ ] More metal chains (iridium, platinum group)
- [ ] In-game config GUI
- [ ] More expert-mode switches

---
*Thanks for playing! Feedback via comments or GitHub Issues.*
