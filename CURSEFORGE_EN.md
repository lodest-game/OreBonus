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
- **IE Crusher** — ore / deepslate ore: yield-base output + chance bonus; crushed ore: fixed dust
  count + configurable bonus chance (default 2 dusts). Energy defaults to vanilla IE (6000 RF).
- **IE Arc Furnace** — shallow/deepslate ore: yield-base ingots + stable slag + chance bonus ingot.
  Time & energy default to vanilla IE (200 ticks / 102400 RF).
- **Zinc line** — new Zinc Dust item: crush crushed zinc for 2 dusts, smelt/blast it into ingots,
  and use it as the brass-alloy additive.
- **Copper hand-mining sync** — hand-mined copper now drops a uniform 1~yield-base random amount
  (default 1-4 shallow / 1-6 deepslate), so machine output is always the manual ceiling;
  Silk Touch & Fortune still work.
- **Dust alloys** (Arc Furnace, stacked single-slot output) — copper dust + 4 dusts → **1 slot × 4 Brass Ingots** + slag;
  cobblestone + 2 dusts → **1 slot × 2 Andesite Alloy** + slag. **Product is fixed; main input, additives,
  output count, slag, time & energy are all user-configurable** (see below).

### 🎚 Fully configurable chances, yields, time & energy (`config/orebonus-common.toml`)

13 knobs per ore: shallow/deepslate bonus chance, nugget chance & count, the shared "yield base"
(used by all three machines and, for copper, the manual-mining ceiling), crushed-ore dust count &
bonus chance, Create crushing time (shallow/deepslate), IE crusher energy, IE arc furnace time &
energy. Chances default to 10%/50%/75%; **time & energy default to the
vanilla mod values**; plus a **global chance multiplier** for one-click difficulty scaling.
Apply with in-game `/reload` — no restart needed.

```toml
[general]
bonusChanceMultiplier = 1.0    # 0.5 = halved rewards (harder), 2.0 = doubled (easier)
disableHammerCrushing = false  # expert-mode switch (below)

[ores.iron]
shallowBonusChance = 0.10          # bonus chance shared by all three machines (shallow)
deepslateBonusChance = 0.50        # bonus chance shared by all three machines (deepslate)
expNuggetChance = 0.75
expNuggetCount = 1
shallowMainCount = 1               # yield base (copper: 4, manual ceiling 1-4)
deepslateMainCount = 1             # yield base (deepslate copper: 6, manual ceiling 1-6)
crushedDustCount = 2               # crushed ore -> fixed dust count
crushedDustBonusChance = 0.0       # crushed ore -> chance for +1 dust
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
and full config documentation: [GitHub (lodest-game)](https://github.com/lodest-game/OreBonus).

## 🗺 Roadmap

- [ ] More metal chains (iridium, platinum group)
- [ ] In-game config GUI
- [ ] More expert-mode switches

---
*Thanks for playing! Feedback via comments or GitHub Issues.*
