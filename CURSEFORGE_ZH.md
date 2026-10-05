# OreBonus（矿石加工产出配置）

**NeoForge 1.21.1 | 矿石加工 | 概率/时长/能耗全可配置 | 专家模式**

把「矿石加工」的产出主动权交还给玩家：重做 Create 粉碎轮与沉浸工程的粉碎机/电弧炉矿石配方，
每种矿石的**概率奖励、处理时长与能耗**全部可通过配置文件调节——从休闲到专家，一个配置文件搞定。

---

## ✨ 功能

### 🔩 完整的矿石加工产线（铁/铜/金/锌/银/铝/铅/镍/铀）

- **Create 粉碎轮**：浅层原矿 → 主产物 + 10% 额外 + 75% 经验粒（+圆石）；深层原矿 → 主产物 + 50% 额外（+深板岩圆石）；铜矿 4/6 主产物、金矿 2 经验粒。基础时长默认与原版一致（250/350/400 tick）。
- **IE 粉碎机**：粗矿 → 稳定 2 粉末；原矿 → 1 粉末 + 10%/50% 概率额外。能耗默认与原版一致（6000 RF）。
- **IE 电弧炉**：浅层/深层原矿 → 1 锭 + 矿渣。时间/能耗默认与原版一致（200 tick / 102400 RF）。
- **锌粉产线**：新增锌粉物品——粉碎粗锌得 2 锌粉，烧炼/高炉得锌锭，兼作黄铜合金添加剂。
- **粉末合金**（电弧炉，单格堆叠输出）：铜粉 + 4 种粉 → **1 格 ×4 黄铜锭** + 矿渣；圆石 + 2 种粉 → **1 格 ×2 安山合金** + 矿渣。**产物固定，主材料/添加剂/数量/矿渣/时间/能耗全部可自定义**（见下方配置）。

### 🎚 概率 / 时长 / 能耗全部可配置（`config/orebonus-common.toml`）

每种矿石 11 个参数：浅层/深层额外概率、经验粒概率与数量、浅层/深层主产物数量、
Create 粉碎轮浅层/深层基础时长、IE 粉碎机能耗、IE 电弧炉时间与能耗。
概率默认 10%/50%/75%，**时长与能耗默认值与原模组完全一致**；
外加**全局概率倍率**一键缩放难度。改完游戏内 `/reload` 即生效，无需重启。

```toml
[general]
bonusChanceMultiplier = 1.0    # 全局概率倍率：0.5 减半更难，2.0 翻倍更简单
disableHammerCrushing = false  # 专家模式开关（见下）

[ores.iron]
shallowBonusChance = 0.10          # 浅层铁矿额外产出概率
deepslateBonusChance = 0.50        # 深层铁矿额外产出概率
expNuggetChance = 0.75             # 粉碎铁矿的经验粒概率
expNuggetCount = 1                 # 经验粒数量（金矿默认 2）
shallowMainCount = 1               # 浅层铁矿主产物数量（铜矿默认 4）
deepslateMainCount = 1             # 深层铁矿主产物数量（深层铜矿默认 6）
createCrushingTimeShallow = 250    # Create 粉碎轮·浅层铁矿基础时长 tick（IE 矿默认 400）
createCrushingTimeDeepslate = 350  # Create 粉碎轮·深层铁矿基础时长 tick（IE 矿默认 400）
ieCrusherEnergy = 6000             # IE 粉碎机能耗 RF（铁矿/深层铁矿/粉碎粗铁共用）
ieArcTime = 200                    # IE 电弧炉冶炼铁矿的时间 tick
ieArcEnergy = 102400               # IE 电弧炉冶炼铁矿的能耗 RF
```

#### 粉末合金完全自定义

黄铜锭 / 安山合金两个电弧炉配方只有两点不变：**必须经电弧炉、产物固定**。
主材料、最多 4 种添加剂、产物数量、是否产矿渣、时间与能耗都可在配置中修改，
也支持 `enabled=false` 直接移除：

```toml
[alloys.brass]                       # 产物固定：黄铜锭
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

### 🏭 专家模式（深度绑定机械动力）

开启 `disableHammerCrushing = true` 后，删除 IE 全部
「矿石/深层矿石/粗矿 + 工程师锤 → 矿物粉末」的工作台合成：
所有粉末都必须走 IE 粉碎机 / Create 粉碎轮产线，沉浸工程与机械动力深度绑定，
适合想做专家整合包的作者。默认关闭，不影响休闲玩家。

---

## 📦 依赖

- **NeoForge 21.1+**（Minecraft 1.21.1）
- **Create 6.0.10+**（可选，但功能主要围绕它）
- **Immersive Engineering 12.4.2+**（可选，同上）

## ❓ FAQ

- **锌粉在哪？** 创造物品栏「材料」页，JEI 搜索“锌粉 / zinc dust”即可找到。
- **改了配置没生效？** 游戏内执行 `/reload`。
- **想恢复原版概率？** 各矿 `shallowBonusChance`/`deepslateBonusChance` 调成 0.75，
  `expNuggetChance` 保持 0.75，与 Create 原版一致（时长/能耗默认已是原版数值）。

## 📜 许可

MIT 开源，代码 100% 原创（未复制任何第三方模组代码）。仓库：
[GitHub 地址占位](https://github.com/) —— 源码、构建指南与完整配置文档见仓库 README。

## 🗺 计划

- [ ] 更多矿物（如铱、铂系）产线
- [ ] 游戏内 GUI 配置界面
- [ ] 更多专家模式开关（如禁用锭→粉的机器路线）

---
*感谢游玩！建议反馈请通过评论区或 GitHub Issues 提交。*
