package com.orebonus;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置文件定义（config/orebonus-common.toml，COMMON 类型：单机与服务器均由服务端决定并同步给客户端）。
 *
 * <p>每个矿物一节，控制其「概率奖励产出」与「处理时长/能耗」；粉末合金配方单独一节，
 * 材料 / 产物数量 / 是否产矿渣均可自定义。改完保存后，在游戏里执行
 * {@code /reload}（或重启游戏）即可生效。</p>
 *
 * <p>设计基准：{@code shallowMainCount} / {@code deepslateMainCount} 是该矿石的「产量基数」，
 * Create 粉碎轮 / IE 粉碎机 / IE 电弧炉共用；铜矿的手工挖掘掉落也会同步为该基数的 1~N 随机
 * （手挖不超过机器上限）。</p>
 *
 * <p>概率默认值：浅层额外 10% / 深层额外 50% / 经验粒 75%；
 * 时长与能耗默认值与原模组一致（Create 粉碎轮 250/350/400 tick、IE 粉碎机 6000 RF、
 * IE 电弧炉 200 tick / 102400 RF）。</p>
 */
public final class OreBonusConfig {

    /** 单个矿物的全部可调参数。 */
    public static final class OreEntry {
        public final String name;
        public final String cnName;      // 中文名（用于配置注释）
        public final String shallowOre;   // 浅层原矿物品
        public final String deepslateOre; // 深层原矿物品
        public final String crushed;      // 对应的「粉碎粗矿」（Create 物品）
        public final String dust;         // 对应的「粉」（IE 粉尘 / 锌粉）
        public final String ingot;        // 对应的锭（电弧炉产出）
        public final boolean cobbleByproduct; // Create 粉碎轮是否附带圆石/深板岩圆石副产物

        // ---- 概率与数量 ----
        public final ModConfigSpec.DoubleValue shallowBonusChance;
        public final ModConfigSpec.DoubleValue deepslateBonusChance;
        public final ModConfigSpec.DoubleValue expNuggetChance;
        public final ModConfigSpec.IntValue expNuggetCount;
        public final ModConfigSpec.IntValue shallowMainCount;
        public final ModConfigSpec.IntValue deepslateMainCount;
        public final ModConfigSpec.IntValue crushedDustCount;
        public final ModConfigSpec.DoubleValue crushedDustBonusChance;

        // ---- 时长与能耗 ----
        public final ModConfigSpec.IntValue createCrushingTimeShallow;
        public final ModConfigSpec.IntValue createCrushingTimeDeepslate;
        public final ModConfigSpec.IntValue ieCrusherEnergy;
        public final ModConfigSpec.IntValue ieArcTime;
        public final ModConfigSpec.IntValue ieArcEnergy;

        OreEntry(ModConfigSpec.Builder b, OreDef d) {
            this.name = d.name;
            this.cnName = d.cnName;
            this.shallowOre = d.shallowOre;
            this.deepslateOre = d.deepslateOre;
            this.crushed = d.crushed;
            this.dust = d.dust;
            this.ingot = d.ingot;
            this.cobbleByproduct = d.cobbleByproduct;

            b.push(d.name);
            this.shallowBonusChance = b
                    .comment(String.format("浅层%s矿的额外产出概率（Create 粉碎轮 / IE 粉碎机 / IE 电弧炉共用，默认 0.10 = 10%%）", d.cnName))
                    .defineInRange("shallowBonusChance", d.shallowBonus, 0.0, 1.0);
            this.deepslateBonusChance = b
                    .comment(String.format("深层%s矿的额外产出概率（三种机器共用，默认 0.50 = 50%%）", d.cnName))
                    .defineInRange("deepslateBonusChance", d.deepslateBonus, 0.0, 1.0);
            this.expNuggetChance = b
                    .comment(String.format("Create 粉碎轮粉碎%s矿产出经验粒的概率（默认 0.75 = 75%%）", d.cnName))
                    .defineInRange("expNuggetChance", d.expChance, 0.0, 1.0);
            this.expNuggetCount = b
                    .comment(String.format("Create 粉碎轮粉碎%s矿的经验粒产出数量（金矿默认 2）", d.cnName))
                    .defineInRange("expNuggetCount", d.expCount, 0, 64);
            this.shallowMainCount = b
                    .comment(String.format("浅层%s矿产量基数（Create 粉碎轮 / IE 粉碎机 / IE 电弧炉共用；铜矿默认 4）。", d.cnName),
                            String.format("同时决定手镐挖掘浅层%s矿的掉落上限（1~此值随机，仅铜矿有随机掉落）。", d.cnName))
                    .defineInRange("shallowMainCount", d.shallowMain, 0, 64);
            this.deepslateMainCount = b
                    .comment(String.format("深层%s矿产量基数（三种机器共用；深层铜矿默认 6）。", d.cnName),
                            String.format("同时决定手镐挖掘深层%s矿的掉落上限（1~此值随机，仅铜矿有随机掉落）。", d.cnName))
                    .defineInRange("deepslateMainCount", d.deepslateMain, 0, 64);
            this.crushedDustCount = b
                    .comment(String.format("IE 粉碎机粉碎「粉碎粗%s」的固定粉末产出数量（默认 2）。", d.cnName))
                    .defineInRange("crushedDustCount", d.crushedCount, 0, 64);
            this.crushedDustBonusChance = b
                    .comment(String.format("IE 粉碎机粉碎「粉碎粗%s」时，概率额外获得 1 个粉末（默认 0.0 = 无额外；受全局概率倍率影响）。", d.cnName))
                    .defineInRange("crushedDustBonusChance", d.crushedBonusChance, 0.0, 1.0);

            this.createCrushingTimeShallow = b
                    .comment(String.format("Create 粉碎轮·浅层%s矿基础处理时长（tick），默认与原版一致（铁/金/铜/锌 250、银/铝/铅/镍/铀 400）。", d.cnName),
                            "实际耗时随机器转速缩放，转速越快实际越快。旧版默认手感为 100。")
                    .defineInRange("createCrushingTimeShallow", d.createTimeShallow, 1, 24000);
            this.createCrushingTimeDeepslate = b
                    .comment(String.format("Create 粉碎轮·深层%s矿基础处理时长（tick），默认与原版一致（铁/金/铜/锌 350、银/铝/铅/镍/铀 400）。", d.cnName),
                            "实际耗时随机器转速缩放。旧版默认手感为 100。")
                    .defineInRange("createCrushingTimeDeepslate", d.createTimeDeepslate, 1, 24000);
            this.ieCrusherEnergy = b
                    .comment(String.format("IE 粉碎机每次操作的能耗（RF），默认与原版原矿粉碎一致（6000）。", d.cnName),
                            String.format("浅层%s矿 / 深层%s矿 / 粉碎粗%s共用此值；旧版默认手感为 3200。", d.cnName, d.cnName, d.cnName),
                            "注意：粉碎机的处理速度固定为 50 tick，无法通过配方调节。")
                    .defineInRange("ieCrusherEnergy", d.crusherEnergy, 1, 10000000);
            this.ieArcTime = b
                    .comment(String.format("IE 电弧炉冶炼%s矿的时间（tick），默认与原版原矿配方一致（200）。旧版默认手感为 100。", d.cnName))
                    .defineInRange("ieArcTime", d.arcTime, 1, 24000);
            this.ieArcEnergy = b
                    .comment(String.format("IE 电弧炉冶炼%s矿的能耗（RF），默认与原版原矿配方一致（102400）。旧版默认手感为 51200。", d.cnName))
                    .defineInRange("ieArcEnergy", d.arcEnergy, 1, 10000000);
            b.pop();
        }
    }

    /** 粉末合金配方（电弧炉）：产物固定，材料/数量/矿渣全部可自定义。 */
    public static final class AlloyEntry {
        public final String key;       // 配置节名与配方 ID 后缀
        public final String cnName;    // 中文名（用于配置注释）
        public final String resultItem; // 固定产物（黄铜锭 / 安山合金）

        public final ModConfigSpec.BooleanValue enabled;
        public final ModConfigSpec.IntValue time;
        public final ModConfigSpec.IntValue energy;
        public final ModConfigSpec.ConfigValue<String> inputItem;
        public final List<ModConfigSpec.ConfigValue<String>> additives; // 最多 4 个添加剂位
        public final ModConfigSpec.IntValue outputCount;
        public final ModConfigSpec.BooleanValue produceSlag;

        AlloyEntry(ModConfigSpec.Builder b, AlloyDef d) {
            this.key = d.key;
            this.cnName = d.cnName;
            this.resultItem = d.resultItem;

            b.push(d.key);
            this.enabled = b
                    .comment(String.format("是否启用「%s」电弧炉合金配方。false = 移除该配方。", d.cnName))
                    .define("enabled", d.enabled);
            this.time = b
                    .comment(String.format("电弧炉冶炼时间（tick），默认 %d。", d.time))
                    .defineInRange("time", d.time, 1, 24000);
            this.energy = b
                    .comment(String.format("电弧炉冶炼能耗（RF），默认 %d。", d.energy))
                    .defineInRange("energy", d.energy, 1, 10000000);
            this.inputItem = b
                    .comment(String.format("主输入材料（物品 ID，如 %s）。留空 = 不生成该配方。", d.inputItem))
                    .define("inputItem", d.inputItem);
            this.additives = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                String defaultAdditive = i < d.additives.length ? d.additives[i] : "";
                String additiveComment = defaultAdditive.isEmpty()
                        ? String.format("添加剂 %d（物品 ID）。留空 = 不使用该添加剂位。", i + 1)
                        : String.format("添加剂 %d（物品 ID，默认 %s）。留空 = 不使用该添加剂位。", i + 1, defaultAdditive);
                this.additives.add(b.comment(additiveComment).define("additive" + (i + 1), defaultAdditive));
            }
            this.outputCount = b
                    .comment(String.format("产物数量（单格堆叠；产物固定为%s，默认 %d 个）。", d.cnName, d.outputCount))
                    .defineInRange("outputCount", d.outputCount, 1, 64);
            this.produceSlag = b
                    .comment("是否产出矿渣（true 时附带 1 个 immersiveengineering:slag）。")
                    .define("produceSlag", d.produceSlag);
            b.pop();
        }
    }

    /**
     * 矿物数据表：物品定义 + 各配置项的默认值。
     * 概率默认值 10%/50%/75%；时长/能耗默认值来自原模组配方
     * （Create 6.0.10 粉碎 250/350/400 tick、IE 12.4.2 粉碎机 6000 RF、电弧炉 200 tick/102400 RF）。
     */
    private record OreDef(
            String name, String cnName, String shallowOre, String deepslateOre, String crushed, String dust, String ingot,
            boolean cobbleByproduct,
            double shallowBonus, double deepslateBonus, double expChance, int expCount,
            int shallowMain, int deepslateMain,
            int crushedCount, double crushedBonusChance,
            int createTimeShallow, int createTimeDeepslate,
            int crusherEnergy, int arcTime, int arcEnergy) {
    }

    private static final List<OreDef> ORE_DEFS = List.of(
            new OreDef("iron", "铁", "minecraft:iron_ore", "minecraft:deepslate_iron_ore",
                    "create:crushed_raw_iron", "immersiveengineering:dust_iron", "minecraft:iron_ingot",
                    true, 0.10, 0.50, 0.75, 1, 1, 1, 2, 0.0, 250, 350, 6000, 200, 102400),
            new OreDef("gold", "金", "minecraft:gold_ore", "minecraft:deepslate_gold_ore",
                    "create:crushed_raw_gold", "immersiveengineering:dust_gold", "minecraft:gold_ingot",
                    true, 0.10, 0.50, 0.75, 2, 1, 1, 2, 0.0, 250, 350, 6000, 200, 102400),
            new OreDef("copper", "铜", "minecraft:copper_ore", "minecraft:deepslate_copper_ore",
                    "create:crushed_raw_copper", "immersiveengineering:dust_copper", "minecraft:copper_ingot",
                    true, 0.10, 0.50, 0.75, 1, 4, 6, 2, 0.0, 250, 350, 6000, 200, 102400),
            new OreDef("zinc", "锌", "create:zinc_ore", "create:deepslate_zinc_ore",
                    "create:crushed_raw_zinc", "orebonus:zinc_dust", "create:zinc_ingot",
                    true, 0.10, 0.50, 0.75, 1, 1, 1, 2, 0.0, 250, 350, 6000, 200, 102400),
            new OreDef("silver", "银", "immersiveengineering:ore_silver", "immersiveengineering:deepslate_ore_silver",
                    "create:crushed_raw_silver", "immersiveengineering:dust_silver", "immersiveengineering:ingot_silver",
                    false, 0.10, 0.50, 0.75, 1, 1, 1, 2, 0.0, 400, 400, 6000, 200, 102400),
            new OreDef("aluminum", "铝", "immersiveengineering:ore_aluminum", "immersiveengineering:deepslate_ore_aluminum",
                    "create:crushed_raw_aluminum", "immersiveengineering:dust_aluminum", "immersiveengineering:ingot_aluminum",
                    false, 0.10, 0.50, 0.75, 1, 1, 1, 2, 0.0, 400, 400, 6000, 200, 102400),
            new OreDef("lead", "铅", "immersiveengineering:ore_lead", "immersiveengineering:deepslate_ore_lead",
                    "create:crushed_raw_lead", "immersiveengineering:dust_lead", "immersiveengineering:ingot_lead",
                    false, 0.10, 0.50, 0.75, 1, 1, 1, 2, 0.0, 400, 400, 6000, 200, 102400),
            new OreDef("nickel", "镍", "immersiveengineering:ore_nickel", "immersiveengineering:deepslate_ore_nickel",
                    "create:crushed_raw_nickel", "immersiveengineering:dust_nickel", "immersiveengineering:ingot_nickel",
                    false, 0.10, 0.50, 0.75, 1, 1, 1, 2, 0.0, 400, 400, 6000, 200, 102400),
            new OreDef("uranium", "铀", "immersiveengineering:ore_uranium", "immersiveengineering:deepslate_ore_uranium",
                    "create:crushed_raw_uranium", "immersiveengineering:dust_uranium", "immersiveengineering:ingot_uranium",
                    false, 0.10, 0.50, 0.75, 1, 1, 1, 2, 0.0, 400, 400, 6000, 200, 102400));

    /** 合金配方默认数据（仅作默认值，用户可自行修改材料/数量/矿渣）。 */
    private record AlloyDef(String key, String cnName, String resultItem, boolean enabled,
                            int time, int energy, String inputItem, String[] additives,
                            int outputCount, boolean produceSlag) {
    }

    private static final List<AlloyDef> ALLOY_DEFS = List.of(
            new AlloyDef("brass", "黄铜锭", "create:brass_ingot", true,
                    200, 102400, "immersiveengineering:dust_copper",
                    new String[]{"orebonus:zinc_dust", "immersiveengineering:dust_steel",
                            "immersiveengineering:dust_aluminum", "immersiveengineering:dust_nickel"},
                    4, true),
            new AlloyDef("andesite", "安山合金", "create:andesite_alloy", true,
                    100, 51200, "minecraft:cobblestone",
                    new String[]{"immersiveengineering:dust_steel", "immersiveengineering:dust_aluminum", "", ""},
                    2, true));

    /** 全局概率倍率：所有概率产出乘以该系数（结果限制在 0~1），相当于整体难度旋钮。 */
    public static final ModConfigSpec.DoubleValue BONUS_CHANCE_MULTIPLIER;

    /** 专家模式：禁用「矿石/深层矿石/粗矿 + 工程师锤 → 粉」的工作台合成（默认关闭）。 */
    public static final ModConfigSpec.BooleanValue DISABLE_HAMMER_CRUSHING;

    public static final Map<String, OreEntry> ORES = new LinkedHashMap<>();

    public static final Map<String, AlloyEntry> ALLOYS = new LinkedHashMap<>();

    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("general");
        BONUS_CHANCE_MULTIPLIER = b
                .comment("全局概率倍率：shallowBonusChance / deepslateBonusChance / expNuggetChance / crushedDustBonusChance 都会乘以该系数（乘积限制在 0~1）。",
                        "例：0.5 表示额外奖励全部减半（更难）；2.0 表示翻倍（更简单，但单值上限 100%）。")
                .defineInRange("bonusChanceMultiplier", 1.0, 0.0, 10.0);
        DISABLE_HAMMER_CRUSHING = b
                .comment("专家模式开关：禁用「矿石 / 深层矿石 / 粗矿 + 工程师锤 = 矿物粉末」的工作台合成",
                        "（即 IE 自带的全部 immersiveengineering:hammer_crushing 配方，覆盖铁/铜/金/锌/铝/铅/银/镍/铀等）。",
                        "默认 false = 不禁用，锤子可直接砸出粉末，玩法自由；",
                        "改为 true 后，所有矿物粉末必须走 IE 粉碎机 / Create 粉碎轮等机器产线，沉浸工程与机械动力深度绑定。",
                        "修改后 /reload 生效。")
                .define("disableHammerCrushing", false);
        b.pop();

        b.push("ores");
        for (OreDef def : ORE_DEFS) {
            ORES.put(def.name(), new OreEntry(b, def));
        }
        b.pop();

        b.push("alloys");
        for (AlloyDef def : ALLOY_DEFS) {
            ALLOYS.put(def.key(), new AlloyEntry(b, def));
        }
        b.pop();

        SPEC = b.build();
    }

    /** 读取概率值并应用全局倍率，限制在 [0, 1]。 */
    public static float chance(ModConfigSpec.DoubleValue value) {
        double raw = value.get() * BONUS_CHANCE_MULTIPLIER.get();
        return (float) Math.min(1.0, Math.max(0.0, raw));
    }

    private OreBonusConfig() {
    }
}
