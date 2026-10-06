package com.orebonus;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 物品注册。
 * 锌粉为本模组的自定义粉末物品，纹理复用沉浸工程的硝石粉材质
 * （见 assets/orebonus/models/item/zinc_dust.json）。
 */
public final class OreBonusItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(OreBonusMod.MOD_ID);

    /** 锌粉：IE 粉碎机粉碎粗锌的产物，可烧炼成锌锭，也用作电弧炉黄铜合金添加剂。 */
    public static final DeferredItem<Item> ZINC_DUST = ITEMS.registerSimpleItem("zinc_dust");

    /**
     * 把锌粉加入创造物品栏「材料」页（同时加入搜索页）。
     * 否则该物品不会出现在创造菜单中，JEI 搜索框也搜不到。
     */
    public static void onBuildCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(new ItemStack(ZINC_DUST.get()), CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
        }
    }

    private OreBonusItems() {
    }
}
