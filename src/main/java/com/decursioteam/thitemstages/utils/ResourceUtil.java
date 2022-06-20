package com.decursioteam.thitemstages.utils;

import com.decursioteam.thitemstages.THItemStages;
import com.decursioteam.thitemstages.datagen.RestrictionsData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

public class ResourceUtil {

    public static List<ItemStack> resourceToIngredient(Set<ResourceLocation> set, String name){
        List<ItemStack> ingredients = new ArrayList<>();
        for (ResourceLocation resourceLocation : set) {
            if(!Objects.equals(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(resourceLocation)).getRegistryName(), new ResourceLocation("minecraft:air"))){
                ingredients.add(new ItemStack(ForgeRegistries.ITEMS.getValue(resourceLocation)));
            }
            else if(!Objects.equals(Objects.requireNonNull(ForgeRegistries.BLOCKS.getValue(resourceLocation)).getRegistryName(), new ResourceLocation("minecraft:air"))){
                ingredients.add(new ItemStack(ForgeRegistries.BLOCKS.getValue(resourceLocation)));
            }
            else THItemStages.LOGGER.error("[T.H.I.S] Invalid resource ID \""+ resourceLocation.toString() + "\" in " + name + " wasn't loaded.");
        }
        return ingredients;
    }
    public static Set<ResourceLocation> getItems(String restriction){
        return new HashSet<>(RestrictionsData.getRestrictionData(restriction).getData().getItemList());
    }

    public static Set<ResourceLocation> getBlocks(String restriction){
        return new HashSet<>(RestrictionsData.getRestrictionData(restriction).getData().getBlockList());
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

    public static Set<ResourceLocation> getDimensions(String restriction){
        return new HashSet<>(RestrictionsData.getRestrictionData(restriction).getData().getDimensionList());
    }

    public static Set<String> getContainers(String restriction){
        return new HashSet<>(RestrictionsData.getRestrictionData(restriction).getData().getContainerList());
    }

    public static boolean checkItem(String restriction, ItemStack itemStack) {
        return getItems(restriction).contains(itemStack.getItem().getRegistryName());
    }

    public static boolean checkBlock(String restriction, ItemStack itemStack) {
        return getItems(restriction).contains(itemStack.getItem().getRegistryName());
    }

    public static boolean checkMod(String restriction, ItemStack itemStack) {
        if(!getMods(restriction).isEmpty() && !getExceptions(restriction).contains(itemStack.getItem().getRegistryName())){
            for (String modID : getMods(restriction)) {
                if (Objects.requireNonNull(itemStack.getItem().getRegistryName()).getNamespace().equals(modID) && !getExceptions(restriction).contains(itemStack.getItem().getRegistryName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean checkTag(String restriction, ItemStack itemStack) {
        Set<ResourceLocation> itemTags = new HashSet<>();
        itemStack.getTags().forEach(s -> {
            itemTags.add(s.location());
        });
        if(!getTags(restriction).isEmpty() && !getExceptions(restriction).contains(itemStack.getItem().getRegistryName())){
            for (ResourceLocation tagID : getTags(restriction)) {
                if(itemTags.contains(tagID)) return true;
            }
        }
        return false;
    }

    public static boolean checkAllItems(String restriction, ItemStack itemStack) {
        if(getExceptions(restriction).isEmpty() || !getExceptions(restriction).contains(itemStack.getItem().getRegistryName())){
            return checkMod(restriction, itemStack) || checkTag(restriction, itemStack) || checkBlock(restriction, itemStack) || checkItem(restriction, itemStack);
        }
        return false;
    }
}
