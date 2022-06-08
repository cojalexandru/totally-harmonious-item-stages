package com.decursioteam.thitemstages;

import com.decursioteam.thitemstages.config.CommonConfig;
import com.decursioteam.thitemstages.datagen.RestrictionsData;
import com.decursioteam.thitemstages.datagen.utils.IStagesData;
import com.decursioteam.thitemstages.events.SyncStagesEvent;
import com.decursioteam.thitemstages.utils.StageUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.common.thread.EffectiveSide;

import java.util.*;

import static com.decursioteam.thitemstages.THItemStages.LOGGER;
import static com.decursioteam.thitemstages.THItemStages.MOD_ID;
import static com.decursioteam.thitemstages.utils.ResourceUtil.resourceToIngredient;

@JeiPlugin
public class THISJEIPlugin implements IModPlugin {

    private IJeiRuntime jeiRuntime;
    private final List<ItemStack> hiddenItems = new ArrayList<>();

    public THISJEIPlugin() {
        if(EffectiveSide.get().isClient()){
            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, SyncStagesEvent.class, e -> this.updateItems(jeiRuntime));
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, false, RecipesUpdatedEvent.class, e -> this.updateItems(jeiRuntime));
        }
    }

    @Override
    public void onRuntimeAvailable (IJeiRuntime jeiRuntime) {
        this.jeiRuntime = jeiRuntime;
    }

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(MOD_ID, "main");
    }

    private void updateItems(IJeiRuntime iJeiRuntime){
        if (this.jeiRuntime != null) {

            final IIngredientManager iIngredientManager = this.jeiRuntime.getIngredientManager();

            restoreItems(iIngredientManager);
            collectItems(iIngredientManager);
            hideItems(iIngredientManager);
        }
    }

    private void restoreItems(IIngredientManager iIngredientManager) {
        if (!this.hiddenItems.isEmpty()) {
            if(CommonConfig.debugMode.get()) LOGGER.warn("[T.H.I.S] - Restoring the following items at runtime: " + this.hiddenItems);
            iIngredientManager.addIngredientsAtRuntime(VanillaTypes.ITEM, this.hiddenItems);
            this.hiddenItems.clear();
        } else if(CommonConfig.debugMode.get()) LOGGER.warn("[T.H.I.S] - There are no items available for restoring to the JEI ingredient list!");
    }

    private void collectItems(IIngredientManager iIngredientManager) {
        if (jeiRuntime != null && iIngredientManager != null) {
            PlayerEntity player = Minecraft.getInstance().player;
            final IStagesData stageData = StageUtil.getPlayerData(player);
            assert stageData != null;
            final ArrayList<String> playerStages = stageData.getStages();

            Registry.getRestrictionsHashSet().forEach((s) -> {
                if (!playerStages.contains(s) && RestrictionsData.getRestrictionData(s).getSettingsCodec().getHideInJEI()) {
                    Set<ResourceLocation> itemList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getItemList());
                    Set<ResourceLocation> blockList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getBlockList());
                    Set<ResourceLocation> tagList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getTagList());
                    Set<String> modList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getModList());
                    Set<ResourceLocation> exceptionList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getExceptionList());

                    //Collect ingredients from mod list
                    if (!modList.isEmpty()) {
                        for (ItemStack itemStack : iIngredientManager.getAllIngredients(VanillaTypes.ITEM)) {
                            for (String modID : modList) {
                                if (Objects.requireNonNull(itemStack.getItem().getRegistryName()).getNamespace().equals(modID) && !exceptionList.contains(itemStack.getItem().getRegistryName())) {
                                    this.hiddenItems.add(itemStack);
                                }
                            }
                        }
                    }

                    if(!tagList.isEmpty()){
                        for (ItemStack itemStack : iIngredientManager.getAllIngredients(VanillaTypes.ITEM)) {
                            if(exceptionList.contains(itemStack.getItem().getRegistryName())) return;
                            for (ResourceLocation tagID : tagList) {
                                if (itemStack.getItem().getTags().contains(tagID) && !this.hiddenItems.contains(itemStack)) {
                                    this.hiddenItems.add(itemStack);
                                }
                            }
                        }
                    }

                    //Collect ingredients from block list
                    if (!resourceToIngredient(blockList, s).isEmpty() && !resourceToIngredient(blockList, s).containsAll(this.hiddenItems)) {
                        this.hiddenItems.addAll(resourceToIngredient(blockList, s));
                    }

                    //Collect ingredients from item list
                    if (!resourceToIngredient(itemList, s).isEmpty() && !resourceToIngredient(itemList, s).containsAll(this.hiddenItems)) {
                        this.hiddenItems.addAll(resourceToIngredient(itemList, s));
                    }
                }
            });
        } else if(CommonConfig.debugMode.get()) LOGGER.warn("[T.H.I.S] - Couldn't collect items that are supposed to be hidden in JEI because JEIRuntime or IngredientManager ar missing! ");
    }

    private void hideItems(IIngredientManager iIngredientManager) {
        if (!this.hiddenItems.isEmpty()) {
            if(CommonConfig.debugMode.get()) LOGGER.warn("[T.H.I.S] - Hiding the following items: " + this.hiddenItems);
            iIngredientManager.removeIngredientsAtRuntime(VanillaTypes.ITEM, this.hiddenItems);
        } else if(CommonConfig.debugMode.get()) LOGGER.warn("[T.H.I.S] - The are no items that are supposed to be hidden in JEI");
    }
}
