package com.orebonus;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 配方替换核心。
 *
 * <p>工作机制（NeoForge 1.21.1）：</p>
 * <ol>
 *   <li>监听 {@link AddReloadListenerEvent}（每次数据包重载 / 进入世界 / {@code /reload} 都会触发）；</li>
 *   <li>追加一个重载监听器，它在原版配方管理器加载完成后运行；</li>
 *   <li>取出当前全部配方，按 ID 删除 Create / IE 的旧配方；</li>
 *   <li>按配置文件数值在内存中构建新配方 JSON，用 {@link OreBonusRecipeParser} 反序列化为真实配方；</li>
 *   <li>通过 {@link RecipeManager#replaceRecipes(Iterable)} 一次性写回。</li>
 * </ol>
 *
 * <p>不依赖任何第三方脚本模组，也不在编译期依赖 Create / Immersive Engineering；
 * 配方 JSON 与这两个模组自身的 data 文件格式逐字段一致（已对照 Create 6.0.10 与 IE 12.4.2 的 jar 内配方验证）。</p>
 */
public final class OreBonusRecipes {

    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new RecipeReloader(event.getServerResources(), event.getRegistryAccess()));
    }

    // ============================================================
    // 重载监听器
    // ============================================================

    private static final class RecipeReloader implements ResourceManagerReloadListener {
        private final ReloadableServerResources resources;
        private final RegistryAccess registryAccess;

        RecipeReloader(ReloadableServerResources resources, RegistryAccess registryAccess) {
            this.resources = resources;
            this.registryAccess = registryAccess;
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            RecipeManager manager = resources.getRecipeManager();

            try {
                // 复制当前配方表（getRecipes() 返回内部 map 的实时视图，必须先拷贝再修改）
                List<RecipeHolder<?>> recipes = new ArrayList<>(manager.getRecipes());

                // 1. 删除 Create / IE 的旧配方；同时清除本模组上一轮重载写入的配方，保证 /reload 幂等；
                //    专家模式开关开启时，额外删除 IE 的「矿石/粗矿 + 工程师锤 → 粉」工作台合成
                boolean disableHammerCrushing = OreBonusConfig.DISABLE_HAMMER_CRUSHING.get();
                recipes.removeIf(holder ->
                        REMOVE_IDS.contains(holder.id())
                                || (disableHammerCrushing && HAMMER_CRUSHING_IDS.contains(holder.id()))
                                || OreBonusMod.MOD_ID.equals(holder.id().getNamespace()));

                // 2. 按配置生成新配方
                int added = 0;
                int failed = 0;
                for (Map.Entry<ResourceLocation, JsonObject> entry : buildAdditions().entrySet()) {
                    try {
                        recipes.add(OreBonusRecipeParser.parse(entry.getKey(), entry.getValue(), registryAccess));
                        added++;
                    } catch (Exception ex) {
                        failed++;
                        OreBonusMod.LOGGER.error("[orebonus] 配方解析失败 {}（物品 ID 无效或缺少 Create / Immersive Engineering 时会出现，已跳过）: {}",
                                entry.getKey(), ex.toString());
                    }
                }

                // 3. 写回
                manager.replaceRecipes(recipes);
                if (failed > 0) {
                    OreBonusMod.LOGGER.warn("[orebonus] 已注入 {} 个配方，{} 个跳过（见上方错误日志）", added, failed);
                } else {
                    OreBonusMod.LOGGER.info("[orebonus] 已注入 {} 个配方（数值读取自 config/orebonus-common.toml，修改后用 /reload 生效）", added);
                }
            } catch (Exception ex) {
                // 任何意外失败都不破坏本次数据包重载：配方管理器保持原版配方表
                OreBonusMod.LOGGER.error("[orebonus] 配方注入失败，本次重载保持原版配方表: {}", ex.toString());
            }
        }
    }

    // ============================================================
    // 删除清单（原版 Create / IE 配方，由本模组配方替代）
    // ============================================================

    private static final Set<ResourceLocation> REMOVE_IDS = buildRemoveIds();

    /** 从矿石表推导删除清单（不存在的 ID 删除无副作用）。 */
    private static Set<ResourceLocation> buildRemoveIds() {
        Set<ResourceLocation> ids = new HashSet<>();
        for (OreBonusConfig.OreEntry ore : OreBonusConfig.ORES.values()) {
            String n = ore.name;
            // Create 粉碎轮（深层配方只有铁/金/铜/锌实际存在，其余为无副作用删除）
            ids.add(createId("crushing/" + n + "_ore"));
            ids.add(createId("crushing/deepslate_" + n + "_ore"));
            // IE 粉碎机：原矿（锌也删除，保证锌矿必然走「锌粉」产线，行为确定）
            ids.add(ieId("crusher/ore_" + n));
            // IE 电弧炉：原矿
            ids.add(ieId("arcfurnace/ore_" + n));
            // IE 粉碎机：锭/粗矿/粗矿块（按“输出=该金属粉”删除；锌除外，保持原状）
            if (!"zinc".equals(n)) {
                ids.add(ieId("crusher/ingot_" + n));
                ids.add(ieId("crusher/raw_ore_" + n));
                ids.add(ieId("crusher/raw_block_" + n));
            }
        }
        return ids;
    }

    /**
     * IE 自带的「工程师锤工作台粉碎」配方（immersiveengineering:hammer_crushing）：
     * 矿石（c:ores/* 标签，天然覆盖浅层+深层）/ 粗矿 → 矿物粉末。
     * 配置文件 general.disableHammerCrushing = true（专家模式）时按 ID 删除。
     */
    private static final Set<ResourceLocation> HAMMER_CRUSHING_IDS = buildHammerCrushingIds();

    private static Set<ResourceLocation> buildHammerCrushingIds() {
        Set<ResourceLocation> ids = new HashSet<>();
        List<String> metals = new ArrayList<>(OreBonusConfig.ORES.keySet());
        // 整合包之外的 IE 自带金属也一并纳入（全局专家模式）
        metals.addAll(List.of("ardite", "cobalt", "osmium", "platinum", "tin", "tungsten"));
        for (String m : metals) {
            ids.add(ieId("crafting/hammercrushing_" + m));
            ids.add(ieId("crafting/raw_hammercrushing_" + m));
        }
        return ids;
    }

    private static ResourceLocation createId(String path) {
        return ResourceLocation.fromNamespaceAndPath("create", path);
    }

    private static ResourceLocation ieId(String path) {
        return ResourceLocation.fromNamespaceAndPath("immersiveengineering", path);
    }

    // ============================================================
    // 新增配方（JSON 由配置动态生成）
    // ============================================================

    private static Map<ResourceLocation, JsonObject> buildAdditions() {
        Map<ResourceLocation, JsonObject> additions = new LinkedHashMap<>();

        for (OreBonusConfig.OreEntry ore : OreBonusConfig.ORES.values()) {
            float shallowBonus = OreBonusConfig.chance(ore.shallowBonusChance);
            float deepslateBonus = OreBonusConfig.chance(ore.deepslateBonusChance);
            float expChance = OreBonusConfig.chance(ore.expNuggetChance);
            int expCount = ore.expNuggetCount.get();
            int shallowMain = ore.shallowMainCount.get();
            int deepslateMain = ore.deepslateMainCount.get();
            int crushedCount = ore.crushedDustCount.get();
            float crushedBonus = OreBonusConfig.chance(ore.crushedDustBonusChance);
            int createTimeShallow = ore.createCrushingTimeShallow.get();
            int createTimeDeepslate = ore.createCrushingTimeDeepslate.get();
            int crusherEnergy = ore.ieCrusherEnergy.get();
            int arcTime = ore.ieArcTime.get();
            int arcEnergy = ore.ieArcEnergy.get();

            // ---- Create 粉碎轮：原矿 → 主产物 + 概率额外 + 经验粒 (+ 圆石副产物) ----
            additions.put(id("create_crushing/" + ore.name + "_ore"),
                    createCrushing(ore.shallowOre, ore.crushed, shallowMain, shallowBonus, expChance, expCount,
                            ore.cobbleByproduct ? "minecraft:cobblestone" : null, createTimeShallow));
            additions.put(id("create_crushing/" + ore.name + "_deepslate_ore"),
                    createCrushing(ore.deepslateOre, ore.crushed, deepslateMain, deepslateBonus, expChance, expCount,
                            ore.cobbleByproduct ? "minecraft:cobbled_deepslate" : null, createTimeDeepslate));

            // ---- IE 粉碎机：原矿 / 深层矿按产量基数产出 + 概率额外；粉碎粗矿 = 固定数量 + 概率额外；三者共用 ieCrusherEnergy ----
            if (crushedCount > 0 || crushedBonus > 0) {
                additions.put(id("ie_crusher/" + ore.name + "_crushed"),
                        ieCrusher(ore.crushed, ore.dust, crushedCount, crushedBonus > 0 ? crushedBonus : null, crusherEnergy));
            }
            additions.put(id("ie_crusher/" + ore.name + "_ore"),
                    ieCrusher(ore.shallowOre, ore.dust, shallowMain, shallowBonus > 0 ? shallowBonus : null, crusherEnergy));
            additions.put(id("ie_crusher/" + ore.name + "_deepslate_ore"),
                    ieCrusher(ore.deepslateOre, ore.dust, deepslateMain, deepslateBonus > 0 ? deepslateBonus : null, crusherEnergy));

            // ---- IE 电弧炉：原矿 → 产量基数个锭 + 矿渣 + 概率额外锭 ----
            additions.put(id("ie_arc/" + ore.name + "_ore"),
                    ieArcFurnace(ore.shallowOre, List.of(new Output(ore.ingot, shallowMain)), List.of(), arcTime, arcEnergy,
                            true, ore.ingot, shallowBonus > 0 ? shallowBonus : null));
            additions.put(id("ie_arc/" + ore.name + "_deepslate_ore"),
                    ieArcFurnace(ore.deepslateOre, List.of(new Output(ore.ingot, deepslateMain)), List.of(), arcTime, arcEnergy,
                            true, ore.ingot, deepslateBonus > 0 ? deepslateBonus : null));
        }

        // ---- 锌粉 → 锌锭（烧炼 / 高炉）----
        additions.put(id("smelting/zinc_dust_to_ingot"),
                furnaceRecipe("minecraft:smelting", "orebonus:zinc_dust", "create:zinc_ingot", 0.7f, 200));
        additions.put(id("blasting/zinc_dust_to_ingot"),
                furnaceRecipe("minecraft:blasting", "orebonus:zinc_dust", "create:zinc_ingot", 0.7f, 100));

        // ---- 电弧炉粉末合金：产物固定（黄铜锭 / 安山合金），材料 / 数量 / 矿渣由配置决定 ----
        for (OreBonusConfig.AlloyEntry alloy : OreBonusConfig.ALLOYS.values()) {
            String input = alloy.inputItem.get().trim();
            if (!alloy.enabled.get() || input.isEmpty()) {
                OreBonusMod.LOGGER.info("[orebonus] 合金配方 {} 已禁用或主输入为空，跳过", alloy.cnName);
                continue;
            }
            List<String> additives = new ArrayList<>();
            for (ModConfigSpec.ConfigValue<String> additive : alloy.additives) {
                String s = additive.get().trim();
                if (!s.isEmpty()) {
                    additives.add(s);
                }
            }
            additions.put(id("ie_arc/alloy_" + alloy.key),
                    ieArcFurnace(input,
                            List.of(new Output(alloy.resultItem, alloy.outputCount.get())),
                            additives,
                            alloy.time.get(), alloy.energy.get(), alloy.produceSlag.get(), null, null));
        }

        return additions;
    }

    // ============================================================
    // JSON 构建辅助（字段名与 Create 6.0.10 / IE 12.4.2 自带配方完全一致）
    // ============================================================

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(OreBonusMod.MOD_ID, path);
    }

    private static JsonObject item(String itemId) {
        JsonObject o = new JsonObject();
        o.addProperty("item", itemId);
        return o;
    }

    /** Create 粉碎轮结果条目：{@code {"id": "...", "count": n, "chance": f}}。 */
    private static JsonObject processingOutput(String itemId, int count, Float chance) {
        JsonObject o = new JsonObject();
        if (count != 1) {
            o.addProperty("count", count);
        }
        if (chance != null) {
            o.addProperty("chance", chance);
        }
        o.addProperty("id", itemId);
        return o;
    }

    private static JsonObject createCrushing(String inputItem, String crushed, int mainCount,
                                             float bonusChance, float expChance, int expCount, String byproduct,
                                             int processingTime) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "create:crushing");

        JsonArray ingredients = new JsonArray();
        ingredients.add(item(inputItem));
        root.add("ingredients", ingredients);

        root.addProperty("processing_time", processingTime);

        JsonArray results = new JsonArray();
        results.add(processingOutput(crushed, mainCount, null));
        results.add(processingOutput(crushed, 1, bonusChance));
        results.add(processingOutput("create:experience_nugget", expCount, expChance));
        if (byproduct != null) {
            results.add(processingOutput(byproduct, 1, null));
        }
        root.add("results", results);
        return root;
    }

    private static JsonObject ieCrusher(String inputItem, String dust, int resultCount, Float bonusChance, int energy) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "immersiveengineering:crusher");
        root.addProperty("energy", energy);
        root.add("input", item(inputItem));
        root.add("result", countedOutput(dust, resultCount));

        if (bonusChance != null) {
            JsonArray secondaries = new JsonArray();
            JsonObject secondary = new JsonObject();
            secondary.addProperty("chance", bonusChance);
            secondary.add("output", item(dust));
            secondaries.add(secondary);
            root.add("secondaries", secondaries);
        }
        return root;
    }

    /** 电弧炉输出条目：物品 + 数量。 */
    private record Output(String item, int count) {
    }

    /** IE 单格堆叠输出：count=1 用普通 item 形式；count&gt;1 用 {@code {"basePredicate": {"item": ...}, "count": N}}
     *  （IE 的 IngredientWithSize codec 格式，与原版单格堆叠等价）。 */
    private static JsonObject countedOutput(String itemId, int count) {
        if (count <= 1) {
            return item(itemId);
        }
        JsonObject o = new JsonObject();
        o.add("basePredicate", item(itemId));
        o.addProperty("count", count);
        return o;
    }

    private static JsonObject ieArcFurnace(String inputItem, List<Output> results, List<String> additives,
                                           int time, int energy, boolean produceSlag,
                                           String secondaryItem, Float secondaryChance) {
        JsonObject root = new JsonObject();
        root.addProperty("type", "immersiveengineering:arc_furnace");

        JsonArray additivesJson = new JsonArray();
        for (String additive : additives) {
            additivesJson.add(item(additive));
        }
        root.add("additives", additivesJson);

        root.addProperty("energy", energy);
        root.add("input", item(inputItem));

        JsonArray resultsJson = new JsonArray();
        for (Output result : results) {
            resultsJson.add(countedOutput(result.item(), result.count()));
        }
        root.add("results", resultsJson);

        // slag 字段在 IE 的 ArcFurnaceRecipe codec 中为 optionalFieldOf（默认 EMPTY）：不产矿渣时直接省略
        if (produceSlag) {
            JsonObject slag = new JsonObject();
            slag.addProperty("item", "immersiveengineering:slag");
            root.add("slag", slag);
        }

        // 概率额外产出（原矿冶炼时按浅层/深层概率额外出一个锭；secondaries 同样为可选字段）
        if (secondaryItem != null && secondaryChance != null && secondaryChance > 0) {
            JsonArray secondaries = new JsonArray();
            JsonObject secondary = new JsonObject();
            secondary.addProperty("chance", secondaryChance);
            secondary.add("output", item(secondaryItem));
            secondaries.add(secondary);
            root.add("secondaries", secondaries);
        }

        root.addProperty("time", time);
        return root;
    }

    private static JsonObject furnaceRecipe(String type, String ingredientItem, String resultItem,
                                            float experience, int cookingTime) {
        JsonObject root = new JsonObject();
        root.addProperty("type", type);
        root.addProperty("category", "misc");
        root.addProperty("cookingtime", cookingTime);
        root.addProperty("experience", experience);
        root.add("ingredient", item(ingredientItem));

        JsonObject result = new JsonObject();
        result.addProperty("id", resultItem);
        root.add("result", result);
        return root;
    }

    private OreBonusRecipes() {
    }
}
