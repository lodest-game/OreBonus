package com.orebonus;

import net.minecraft.advancements.critereon.EnchantmentPredicate;
import net.minecraft.advancements.critereon.ItemEnchantmentsPredicate;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.ItemSubPredicates;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.ApplyExplosionDecay;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.event.LootTableLoadEvent;

import java.util.List;

/**
 * 铜矿手工挖掘掉落联动：
 * 手镐挖掘铜矿 / 深层铜矿掉落「1 ~ 配置文件中的产量基数」个粗铜（均匀随机），
 * 使粉碎轮的产量始终是手挖的上限，避免手动挖掘倒挂。
 *
 * <p>通过 {@link LootTableLoadEvent} 在数据包重载时替换原版战利品表（与原版表同构，
 * 保留精准采集 / 时运 / 爆炸消失逻辑）；修改配置后 {@code /reload} 即与配方同步生效。</p>
 */
public final class OreBonusLoot {

    private static final ResourceLocation COPPER_ORE = ResourceLocation.withDefaultNamespace("blocks/copper_ore");
    private static final ResourceLocation DEEPSLATE_COPPER_ORE = ResourceLocation.withDefaultNamespace("blocks/deepslate_copper_ore");

    public static void onLootTableLoad(LootTableLoadEvent event) {
        if (!COPPER_ORE.equals(event.getName()) && !DEEPSLATE_COPPER_ORE.equals(event.getName())) {
            return;
        }
        OreBonusConfig.OreEntry copper = OreBonusConfig.ORES.get("copper");
        if (copper == null) {
            return;
        }
        try {
            if (COPPER_ORE.equals(event.getName())) {
                event.setTable(copperLoot(event.getRegistries(), Blocks.COPPER_ORE.asItem(), copper.shallowMainCount.get()));
            } else {
                event.setTable(copperLoot(event.getRegistries(), Blocks.DEEPSLATE_COPPER_ORE.asItem(), copper.deepslateMainCount.get()));
            }
        } catch (Exception ex) {
            // 注册表异常等意外情况：保留原版战利品表，不影响数据包重载
            OreBonusMod.LOGGER.warn("[orebonus] 铜矿掉落表替换失败，保留原版掉落: {}", ex.toString());
        }
    }

    /** 与原版铜矿表同构：精准采集→矿石本体；否则→均匀 1~max 个粗铜 + 时运加成 + 爆炸消失。 */
    private static LootTable copperLoot(HolderLookup.Provider registries, Item oreBlock, int maxCount) {
        int max = Math.max(1, maxCount);
        HolderLookup.RegistryLookup<Enchantment> enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT);
        Holder<Enchantment> fortune = enchantments.getOrThrow(Enchantments.FORTUNE);
        Holder<Enchantment> silkTouch = enchantments.getOrThrow(Enchantments.SILK_TOUCH);

        return LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ConstantValue.exactly(1.0f))
                        .add(AlternativesEntry.alternatives(
                                LootItem.lootTableItem(oreBlock)
                                        .when(MatchTool.toolMatches(ItemPredicate.Builder.item()
                                                .withSubPredicate(ItemSubPredicates.ENCHANTMENTS,
                                                        ItemEnchantmentsPredicate.enchantments(List.of(
                                                                new EnchantmentPredicate(silkTouch, MinMaxBounds.Ints.atLeast(1))))))),
                                LootItem.lootTableItem(Items.RAW_COPPER)
                                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(1, max)))
                                        .apply(ApplyBonusCount.addOreBonusCount(fortune))
                                        .apply(ApplyExplosionDecay.explosionDecay()))))
                .build();
    }

    private OreBonusLoot() {
    }
}
