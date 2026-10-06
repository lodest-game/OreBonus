package com.orebonus;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

/**
 * OreBonus —— 矿石加工产出配置模组（NeoForge 1.21.1）：
 * <ul>
 *   <li>注册锌粉物品（{@link OreBonusItems}），配套粉碎 / 烧炼产线；</li>
 *   <li>重做 Create 粉碎轮 / IE 粉碎机 / IE 电弧炉的矿石配方（{@link OreBonusRecipes}）；</li>
 *   <li>铜矿手挖掉落与粉碎轮产量联动，机器产量恒为手挖上限（{@link OreBonusLoot}）；</li>
 *   <li>电弧炉粉末合金配方：材料 / 产物数量 / 是否产矿渣均可自定义；</li>
 *   <li>所有概率奖励、处理时长与能耗都可在 {@code config/orebonus-common.toml} 中按矿石调整
 *       （{@link OreBonusConfig}），并内置专家模式开关。</li>
 * </ul>
 */
@Mod(OreBonusMod.MOD_ID)
public final class OreBonusMod {

    public static final String MOD_ID = "orebonus";
    public static final Logger LOGGER = LogUtils.getLogger();

    public OreBonusMod(IEventBus modEventBus) {
        // 1. 注册自定义物品（锌粉）
        OreBonusItems.ITEMS.register(modEventBus);

        // 2. 注册通用配置文件（config/orebonus-common.toml）
        ModList.get().getModContainerById(MOD_ID)
                .ifPresent(c -> c.registerConfig(ModConfig.Type.COMMON, OreBonusConfig.SPEC));

        // 3. 把锌粉加入创造物品栏（材料页 + 搜索页），JEI 才能搜到
        modEventBus.addListener(OreBonusItems::onBuildCreativeTab);

        // 4. 在数据包重载时注入/替换配方
        NeoForge.EVENT_BUS.addListener(OreBonusRecipes::onAddReloadListener);

        // 5. 铜矿手挖掉落与产量基数联动
        NeoForge.EVENT_BUS.addListener(OreBonusLoot::onLootTableLoad);
    }
}
