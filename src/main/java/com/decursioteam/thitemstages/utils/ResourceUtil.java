package com.decursioteam.thitemstages.utils;

import com.decursioteam.thitemstages.THItemStages;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class ResourceUtil {

    public static List<ItemStack> resourceToIngredient(Set<ResourceLocation> set, String name){
        List<ItemStack> ingredients = new ArrayList<>();
        for (ResourceLocation resourceLocation : set) {
            if(!Objects.equals(Objects.requireNonNull(ForgeRegistries.ITEMS.getValue(resourceLocation)).getRegistryName(), new ResourceLocation("minecraft:air"))){
                ingredients.add(new ItemStack(ForgeRegistries.ITEMS.getValue(resourceLocation)));
            } else THItemStages.LOGGER.error("[T.H.I.S] Invalid resource ID \""+ resourceLocation.toString() + "\" in " + name + " wasn't loaded.");
        }
        return ingredients;
    }
}
