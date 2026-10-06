package com.orebonus;

import com.google.gson.JsonObject;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

/**
 * {@link RecipeManager#fromJson(ResourceLocation, JsonObject, HolderLookup.Provider)}
 * 在 NeoForge 1.21.1 中是 protected static 方法：它使用 {@code Recipe.CODEC}
 * 按 JSON 里的 {@code type} 字段查找配方序列化器（Create / IE 注册的序列化器也在其中），
 * 与游戏自身加载数据包配方走完全相同的解析路径。
 *
 * <p>通过子类把它暴露出来，即可在运行时把「读取自配置文件的 JSON」反序列化为真正的配方对象。
 * 本类永远不会被实例化。</p>
 */
public final class OreBonusRecipeParser extends RecipeManager {

    private OreBonusRecipeParser() {
        super((HolderLookup.Provider) null);
    }

    /** 把运行时构建的配方 JSON 解析为 RecipeHolder。解析失败会抛出异常，由调用方捕获并记录日志。 */
    public static RecipeHolder<?> parse(ResourceLocation id, JsonObject json, HolderLookup.Provider registries) {
        return fromJson(id, json, registries);
    }
}
