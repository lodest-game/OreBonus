# OreBonus —— 矿石加工产出的可配置模组

> 一个面向 NeoForge 1.21.1 的矿石加工强化模组：重做 Create 粉碎轮 / 沉浸工程粉碎机与电弧炉的
> 全部矿石配方，**概率产出、处理时长与能耗都可在配置文件中按矿石调节**，粉末合金配方可自定义，
> 并内置「专家模式」开关，让沉浸工程与机械动力深度绑定。

---

## 功能一览

- **锌粉产线**：新增锌粉物品（`orebonus:zinc_dust`）。IE 粉碎机粉碎粗锌 → 2 锌粉；锌粉可烧炼 /
  高炉熔炼成锌锭；锌粉还用作电弧炉黄铜合金的添加剂。
- **Create 粉碎轮配方重做**（铁/铜/金/锌/银/铝/铅/镍/铀 9 种矿石）：
  - 浅层原矿 → 主产物 + 10% 概率额外 + 75% 经验粒（+ 圆石副产物）；
  - 深层原矿 → 主产物 + 50% 概率额外 + 75% 经验粒（+ 深板岩圆石副产物）；
  - 铜矿主产物 4 个 / 深层 6 个；金矿经验粒 2 个——全部数值可配置。
- **IE 粉碎机配方重做**：粗矿 → 稳定 2 粉末；浅层原矿 → 1 粉末 + 10% 额外；
  深层原矿 → 1 粉末 + 50% 额外（能耗默认与原版一致：6000 RF）。
- **IE 电弧炉配方重做**：浅层/深层原矿 → 1 锭 + 矿渣（默认与原版一致：200 tick / 102400 RF），
  覆盖原版三种矿石与 IE 五种矿石。
- **粉末合金**（电弧炉，单格堆叠输出，完全自定义）：
  - 默认：铜粉 + 锌粉/钢粉/铝粉/镍粉 → 1 格 ×4 黄铜锭 + 1 格矿渣；圆石 + 钢粉/铝粉 → 1 格 ×2 安山合金 + 1 格矿渣；
  - 产物固定为黄铜锭 / 安山合金，主材料、最多 4 种添加剂、产物数量、是否产矿渣、时间与能耗、
    是否启用全部可在 `[alloys.*]` 配置节中修改。
- **可配置难度**：每种矿石 11 个参数——浅层/深层额外概率、经验粒概率与数量、主产物数量、
  Create 粉碎轮浅层/深层基础时长、IE 粉碎机能耗、IE 电弧炉时间与能耗（时长/能耗默认与原模组一致）；
  另有全局概率倍率一键缩放；修改后游戏内 `/reload` 即生效。
- **专家模式开关**：`disableHammerCrushing = true` 时删除 IE 全部
  「矿石/粗矿 + 工程师锤 → 粉末」工作台合成，粉末必须走机器产线。
- **完善的物品可见性**：锌粉已加入创造物品栏「材料」页与搜索页，JEI 可直接搜到。

## 依赖与兼容

| 项 | 要求 |
|---|---|
| 模组加载器 | NeoForge 21.1+（Minecraft 1.21.1） |
| Create | 6.0.10+（必须，本模组功能主要围绕它） |
| Immersive Engineering | 12.4.2+（必须，同上） |

模组无任何第三方脚本依赖，编译期也不依赖 Create / IE；配方通过原版 `RecipeManager` 在
数据包重载时注入，未安装 Create / IE 时可安全加载（仅跳过对应配方）。

## 安装

1. 把 `orebonus-1.0.0.jar` 放入 `.minecraft/mods`；
2. 进入游戏，日志出现「[orebonus] 已注入 69 个配方」即生效。

## 配置文件

首次启动后自动生成 `config/orebonus-common.toml`（服务端决定并同步给客户端）：

```toml
[general]
	# 全局概率倍率：所有概率产出乘以该系数（结果限制在 0~100%）
	# 0.5 = 全部减半（更难）；2.0 = 翻倍（更简单）
	bonusChanceMultiplier = 1.0

	# 专家模式：true = 删除全部「矿石/深层矿石/粗矿 + 工程师锤 = 粉末」工作台合成
	disableHammerCrushing = false

[ores.iron]   # iron/gold/copper/zinc/silver/aluminum/lead/nickel/uranium 各一节（生成时注释会带上金属名）
	shallowBonusChance = 0.10          # 浅层铁矿额外产出概率（Create 粉碎轮与 IE 粉碎机共用）
	deepslateBonusChance = 0.50        # 深层铁矿额外产出概率
	expNuggetChance = 0.75             # Create 粉碎轮粉碎铁矿产出经验粒的概率
	expNuggetCount = 1                 # 经验粒数量（金矿默认 2）
	shallowMainCount = 1               # 浅层铁矿主产物数量（铜矿默认 4）
	deepslateMainCount = 1             # 深层铁矿主产物数量（深层铜矿默认 6）

	createCrushingTimeShallow = 250    # Create 粉碎轮·浅层铁矿基础时长 tick（原版：铁/金/铜/锌 250，IE 矿 400）
	createCrushingTimeDeepslate = 350  # Create 粉碎轮·深层铁矿基础时长 tick（原版：铁/金/铜/锌 350，IE 矿 400）
	ieCrusherEnergy = 6000             # IE 粉碎机每次操作能耗 RF（浅层铁矿/深层铁矿/粉碎粗铁共用；原版 6000）
	ieArcTime = 200                    # IE 电弧炉冶炼铁矿的时间 tick（原版 200）
	ieArcEnergy = 102400               # IE 电弧炉冶炼铁矿的能耗 RF（原版 102400）
```

修改后保存，游戏内执行 **`/reload`** 即生效，无需重启。

> 概率默认 10%/50%/75%；时长/能耗默认与原模组一致。若想还原旧版默认手感：
> `createCrushingTimeShallow=100`、`createCrushingTimeDeepslate=100`、`ieCrusherEnergy=3200`、
> `ieArcTime=100`、`ieArcEnergy=51200`。

### 粉末合金配方（产物固定，其余全可自定义）

```toml
[alloys.brass]                          # 产物固定：黄铜锭
	enabled = true                      # false = 移除该配方
	time = 200                          # 电弧炉时间 tick
	energy = 102400                     # 电弧炉能耗 RF
	inputItem = "immersiveengineering:dust_copper"   # 主输入材料（留空 = 不生成配方）
	additive1 = "orebonus:zinc_dust"    # 添加剂 1~4（留空 = 不使用）
	additive2 = "immersiveengineering:dust_steel"
	additive3 = "immersiveengineering:dust_aluminum"
	additive4 = "immersiveengineering:dust_nickel"
	outputCount = 4                     # 黄铜锭数量（单格堆叠）
	produceSlag = true                  # 是否附带矿渣

[alloys.andesite]                       # 产物固定：安山合金
	enabled = true
	time = 100
	energy = 51200
	inputItem = "minecraft:cobblestone"
	additive1 = "immersiveengineering:dust_steel"
	additive2 = "immersiveengineering:dust_aluminum"
	additive3 = ""
	additive4 = ""
	outputCount = 2
	produceSlag = true
```

两个合金配方只有两点不变：**必须经电弧炉冶炼、产物固定为黄铜锭/安山合金**；
主材料、添加剂（最多 4 种）、产物数量、是否产矿渣、时间与能耗都可自行修改，
上面只是默认配方。

### 难度调节示例

| 目标 | 改法 |
|---|---|
| 原版体验 | 各矿 `shallowBonusChance=0.75`、`deepslateBonusChance=0.75` |
| 默认（概率 10%/50%/75%，时长/能耗与原模组一致） | 不改 |
| 还原旧版默认手感 | 各矿 `createCrushingTimeShallow/Deepslate=100`、`ieCrusherEnergy=3200`、`ieArcTime=100`、`ieArcEnergy=51200` |
| 更硬核 | `bonusChanceMultiplier=0.3`（或各概率调成 0.05）、粉碎轮时长调大 |
| 更轻松 | `bonusChanceMultiplier=2.0`（单值上限自动限制在 100%）、粉碎轮时长调小 |
| 铜矿不再“高产” | `[ores.copper] shallowMainCount=2, deepslateMainCount=3` |
| 专家模式（禁锤子砸粉） | `[general] disableHammerCrushing=true` |

## 构建

- 环境：JDK 21 + 网络（首次构建需下载 Gradle 9.2.1、NeoForge 21.1.250 与 Minecraft 依赖）
- 命令：`gradlew build`，产物在 `build/libs/orebonus-1.0.0.jar`
- 开发调试：`gradlew runClient`

> ⚠️ 项目路径**不要含中文**：JVM 按系统默认编码读取 Gradle worker 的参数文件，
> 路径含中文时 worker 会报 `ClassNotFoundException: worker.org.gradle.process.internal.worker.GradleWorkerMain`。
> 请把项目放到纯英文路径（如 `F:\HMCL\orebonus-mod`）再构建。

## 常见问题

- **JEI 看不到新配方？** JEI 需要配方表刷新，进一次存档或 `/reload` 后再打开。
- **改了配置没生效？** 确认改的是 `config/orebonus-common.toml`，然后 `/reload`。
- **没装 Create / IE？** 模组照常加载，只跳过对应配方；锌粉物品仍会注册。
- **日志报配方解析失败？** 检查 Create ≥ 6.0.10、IE ≥ 12.4.2；若涉及合金配方，同时检查
  `[alloys.*]` 里填写的物品 ID 是否有效。
- **想改处理时长/能量？** 矿石类配方直接改对应矿石的 `createCrushingTime*` / `ieCrusherEnergy` /
  `ieArcTime` / `ieArcEnergy`；粉末合金改 `[alloys.*]` 中的 `time` / `energy`。全部无需重新构建。

## 许可与代码来源

- 许可：**MIT**（见 LICENSE）。
- 全部 Java / JSON / 文档代码为原创，未复制任何第三方模组代码；
  `gradlew` 等 wrapper 文件来自 Gradle 官方（Apache-2.0，文件头保留）；
  构建脚本遵循 NeoForge 官方 MDK 模板（其存在目的即模组起始模板）。
