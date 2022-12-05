package com.decursioteam.thitemstages.utils;

import com.decursioteam.thitemstages.datagen.RestrictionsData;
import com.decursioteam.thitemstages.restrictions.DimensionRestriction;
import com.decursioteam.thitemstages.restrictions.ItemRestriction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ResourceUtil {
    public static Set<ItemRestriction> getItems(String restriction){
        return new HashSet<>(RestrictionsData.getRestrictionData(restriction).getData().getItemList());
    }

    public static Set<ResourceLocation> getTags(String restriction){
        return new HashSet<>(RestrictionsData.getRestrictionData(restriction).getData().getTagList());
    }

    public static Set<String> getMods(String restriction){
        return new HashSet<>(RestrictionsData.getRestrictionData(restriction).getData().getModList());
    }

    public static Set<ResourceLocation> getExceptions(String restriction){
        Set<ResourceLocation> exceptionList = new HashSet<>(RestrictionsData.getRestrictionData(restriction).getData().getExceptionList());
        exceptionList.add(new ResourceLocation("minecraft:air"));
        return exceptionList;
    }

    public static Set<DimensionRestriction> getDimensions(String restriction){
        return new HashSet<>(RestrictionsData.getRestrictionData(restriction).getData().getDimensionList());
    }

    public static Set<String> getContainers(String restriction){
        return new HashSet<>(RestrictionsData.getRestrictionData(restriction).getData().getContainerList());
    }

    public static ResourceLocation getRegistryName(Item item)
    {
        return ForgeRegistries.ITEMS.getKey(item);
    }

    public static boolean check(String restriction, ItemStack itemStack, CHECK_TYPES checkType) {
        switch(checkType)
        {
            case ITEM: {
                boolean pass = false;
                for (ItemRestriction item : getItems(restriction)) {
                    if(item.getCompoundNBT() == null) {
                        if(item.getItemStack().sameItem(itemStack)) pass = true;
                    }
                    else if(itemStack.getTag() != null) {
                        if(item.getItemStack().sameItem(itemStack) && item.getCompoundNBT().toString().equals(itemStack.getTag().toString())) pass = true;
                    }
                }
                return pass;
            }
            case MOD: {
                if(!getMods(restriction).isEmpty() && !getExceptions(restriction).contains(getRegistryName(itemStack.getItem()))) {
                    for (String modID : getMods(restriction)) {
                        if (Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(itemStack.getItem())).getNamespace().equals(modID) && !getExceptions(restriction).contains(getRegistryName(itemStack.getItem()))) {
                            return true;
                        }
                    }
                }
                return false;
            }
            case TAG: {
                if(!getTags(restriction).isEmpty() && !getExceptions(restriction).contains(getRegistryName(itemStack.getItem()))) {
                    for (ResourceLocation tagID : getTags(restriction)) {
                        if (itemStack.is(Objects.requireNonNull(ForgeRegistries.ITEMS.tags()).createTagKey(tagID))) {
                            return true;
                        }
                    }
                }
                return false;
            }
            case ALL: {
                return check(restriction, itemStack, CHECK_TYPES.ITEM) ||
                        check(restriction, itemStack, CHECK_TYPES.MOD) ||
                        check(restriction, itemStack, CHECK_TYPES.TAG);
            }
            default:
                return false;
        }
    }

    public enum CHECK_TYPES {
        MOD,
        ITEM,
        TAG,
        ALL
    }
}
