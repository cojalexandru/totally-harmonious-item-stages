package com.decursioteam.decursio_stages.utils;

import com.decursioteam.decursio_stages.DecursioStages;
import com.decursioteam.decursio_stages.datagen.RestrictionsData;
import com.decursioteam.decursio_stages.restrictions.DimensionRestriction;
import com.decursioteam.decursio_stages.restrictions.ItemExclusion;
import com.decursioteam.decursio_stages.restrictions.ItemRestriction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

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

    public static Set<ItemExclusion> getExceptions(String restriction){
        return new HashSet<>(RestrictionsData.getRestrictionData(restriction).getData().getExceptionList());
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
        if(getRegistryName(itemStack.getItem()).equals(new ResourceLocation("minecraft:air"))) return false;
        switch(checkType)
        {
            case ITEM: {
                boolean pass = false;
                for (ItemRestriction item : getItems(restriction)) {
                    if(item.getCompoundNBT() == null) {
                        if(item.getItemStack().is(itemStack.getItem())) pass = true;
                    }
                    else if(itemStack.getTag() != null) {
                        if(item.getItemStack().is(itemStack.getItem()) && NBTComparisonUtil.areNBTCompoundsEqual(item.getCompoundNBT(), itemStack.getTag())) pass = true;
                    }
                }
                return pass;
            }
            case MOD: {
                if(!getMods(restriction).isEmpty() && isItemExcluded(restriction, itemStack)) {
                    for (String modID : getMods(restriction)) {
                        if (Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(itemStack.getItem())).getNamespace().equals(modID) && isItemExcluded(restriction, itemStack)) {
                            return true;
                        }
                    }
                }
                return false;
            }
            case TAG: {
                if(!getTags(restriction).isEmpty() && isItemExcluded(restriction, itemStack)) {
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

    public static boolean isItemExcluded(String restriction, ItemStack itemStack)
    {
        AtomicBoolean pass = new AtomicBoolean(true);
        getExceptions(restriction).forEach(itemExclusion -> {
            if (itemExclusion.getResourceLocation() != null) {
                if(itemExclusion.getCompoundNBT() == null) {
                    if(itemExclusion.getItemStack().is(itemStack.getItem())) pass.set(false);
                }
                else if(itemStack.getTag() != null) {
                    if(itemExclusion.getItemStack().is(itemStack.getItem()) && itemExclusion.getCompoundNBT().toString().equals(itemStack.getTag().toString())) pass.set(false);
                }
            }
            else if(itemExclusion.getMod() != null) {
                if(getRegistryName(itemStack.getItem()).getNamespace().equals(itemExclusion.getMod())) pass.set(false);
            }
            else if(itemExclusion.getTag() != null) {
                if(itemStack.is(Objects.requireNonNull(ForgeRegistries.ITEMS.tags()).createTagKey(itemExclusion.getTag()))) pass.set(false);
            }
        });
        return pass.get();
    }

    public enum CHECK_TYPES {
        MOD,
        ITEM,
        TAG,
        ALL
    }
}
