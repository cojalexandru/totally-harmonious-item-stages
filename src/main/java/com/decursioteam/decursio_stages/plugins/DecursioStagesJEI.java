package com.decursioteam.decursio_stages.plugins;

import com.decursioteam.decursio_stages.Registry;
import com.decursioteam.decursio_stages.config.CommonConfig;
import com.decursioteam.decursio_stages.datagen.RestrictionsData;
import com.decursioteam.decursio_stages.datagen.utils.IStagesData;
import com.decursioteam.decursio_stages.events.SyncStagesEvent;
import com.decursioteam.decursio_stages.utils.StageUtil;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.util.thread.EffectiveSide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.decursioteam.decursio_stages.DecursioStages.LOGGER;
import static com.decursioteam.decursio_stages.DecursioStages.MOD_ID;
import static com.decursioteam.decursio_stages.utils.ResourceUtil.*;

@JeiPlugin
public class DecursioStagesJEI implements IModPlugin {

    private final List<ItemStack> hiddenItems = new ArrayList<>();
    private final HashMap<Recipe<?>, ResourceLocation> hiddenRecipes = new HashMap<>();
    private IJeiRuntime jeiRuntime;

    public DecursioStagesJEI() {
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
            if(CommonConfig.debugMode.get()) LOGGER.info("Restoring the following items at runtime: " + this.hiddenItems);
            iIngredientManager.addIngredientsAtRuntime(VanillaTypes.ITEM_STACK, this.hiddenItems);
            this.hiddenItems.clear();
        } else if(CommonConfig.debugMode.get()) LOGGER.warn("There are no items available for restoring to the JEI ingredient list!");
    }

    private void collectItems(IIngredientManager iIngredientManager) {
        if (jeiRuntime != null && iIngredientManager != null) {
            final Player player = Minecraft.getInstance().player;
            final IStagesData stageData = StageUtil.getPlayerData(player);
            try {
                Registry.getRestrictionsHashSet().forEach((s) -> {
                    String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                    if (!stageData.hasStage(stage) && RestrictionsData.getRestrictionData(s).getSettingsCodec().getHideInJEI()) {
                        //Collect ingredients from mod list
                        if (!getMods(s).isEmpty()) {
                            for (ItemStack itemStack : iIngredientManager.getAllIngredients(VanillaTypes.ITEM_STACK)) {
                                if(check(s, itemStack, CHECK_TYPES.MOD)) add(itemStack);
                            }
                        }

                        //Collect ingredients from tag list
                        if(!getTags(s).isEmpty()) {
                            for (ItemStack itemStack : iIngredientManager.getAllIngredients(VanillaTypes.ITEM_STACK)) {
                                if(check(s, itemStack, CHECK_TYPES.TAG)) add(itemStack);
                            }
                        }

                        //Collect ingredients from item list
                        if (!getItems(s).isEmpty()) {
                            for (ItemStack itemStack : iIngredientManager.getAllIngredients(VanillaTypes.ITEM_STACK)) {
                                if(check(s, itemStack, CHECK_TYPES.ITEM)) {
                                    add(itemStack);
                                }
                            }
                        }
                    }
                });
            }
            catch (NullPointerException e){
                //
            }
        } else if(CommonConfig.debugMode.get()) LOGGER.error("Couldn't collect items that are supposed to be hidden in JEI because JEIRuntime or IngredientManager ar missing! ");
    }

    private void hideItems(IIngredientManager iIngredientManager) {
        if (!this.hiddenItems.isEmpty()) {
            if(CommonConfig.debugMode.get()) LOGGER.info("Hiding the following items: " + this.hiddenItems);
            iIngredientManager.removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, this.hiddenItems);
        } else if(CommonConfig.debugMode.get()) LOGGER.warn("The are no items that are supposed to be hidden in JEI");
    }

    private void add(ItemStack itemStack) {
        this.hiddenItems.add(itemStack);
    }

    private void add(List<ItemStack> itemStacks) {
        this.hiddenItems.addAll(itemStacks);
    }
}