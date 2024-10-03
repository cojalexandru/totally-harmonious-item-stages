package com.decursioteam.decursio_stages.plugins;

import com.decursioteam.decursio_stages.Registry;
import com.decursioteam.decursio_stages.config.CommonConfig;
import com.decursioteam.decursio_stages.datagen.RestrictionsData;
import com.decursioteam.decursio_stages.datagen.utils.IStagesData;
import com.decursioteam.decursio_stages.events.SyncStagesEvent;
import com.decursioteam.decursio_stages.utils.ResourceUtil;
import com.decursioteam.decursio_stages.utils.StageUtil;
import me.shedaniel.rei.api.client.entry.filtering.FilteringRuleTypeRegistry;
import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.entry.EntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

import static com.decursioteam.decursio_stages.DecursioStages.LOGGER;
import static com.decursioteam.decursio_stages.utils.ResourceUtil.*;

@REIPluginClient
public class DecursioStagesREI implements REIClientPlugin {

    private final List<EntryStack<?>> hiddenItems = new ArrayList<>();

    public DecursioStagesREI() {
        if(EffectiveSide.get().isClient()){
            MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, SyncStagesEvent.class, e -> this.updateItems());
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOW, false, RecipesUpdatedEvent.class, e -> this.updateItems());
        }
    }

    @Override
    public void registerBasicEntryFiltering(BasicFilteringRule<?> rule) {
        updateItems();
    }

    private void updateItems() {
        EntryRegistry entryRegistry = EntryRegistry.getInstance();
        BasicFilteringRule<?> rule = FilteringRuleTypeRegistry.getInstance().basic();

        restoreItems(rule);
        collectItems(entryRegistry);
        hideItems(rule);
    }

    private void restoreItems(BasicFilteringRule<?> rule) {
        if (!this.hiddenItems.isEmpty()) {
            if(CommonConfig.debugMode.get()) LOGGER.info("Restoring the following items at runtime: " + this.hiddenItems);
            for (EntryStack<?> stack : this.hiddenItems) {
                rule.show(stack);
            }
            this.hiddenItems.clear();
        } else if(CommonConfig.debugMode.get()) LOGGER.warn("There are no items available for restoring to the REI ingredient list!");
    }

    private void collectItems(EntryRegistry registry) {
        registry.getEntryStacks().forEach(entryStack -> {
            final Player player = Minecraft.getInstance().player;
            final IStagesData stageData = StageUtil.getPlayerData(player);
            try {
                Registry.getRestrictionsHashSet().forEach((s) -> {
                    String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                    assert stageData != null;
                    if (!stageData.hasStage(stage) && RestrictionsData.getRestrictionData(s).getSettingsCodec().getHideInJEI()) {
                        if(ForgeRegistries.ITEMS.getDelegate(entryStack.getIdentifier()).isPresent())
                        {
                            ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getDelegate(entryStack.getIdentifier()).get());
                            if (!getMods(s).isEmpty()) {
                                if(check(s, stack, ResourceUtil.CHECK_TYPES.MOD)) {
                                    add(entryStack);
                                }
                            }
                            if(!getTags(s).isEmpty()) {
                                if(check(s, stack, CHECK_TYPES.TAG)) {
                                    add(entryStack);
                                }
                            }
                            if (!getItems(s).isEmpty()) {
                                if (check(s, stack, CHECK_TYPES.ITEM)) {
                                    add(entryStack);
                                }
                            }
                        }
                    }
                });
            }
            catch (NullPointerException e){
                //
            }
        });
    }

    private void hideItems(BasicFilteringRule<?> rule) {
        if (!this.hiddenItems.isEmpty()) {
            if(CommonConfig.debugMode.get()) LOGGER.info("Hiding the following items: " + this.hiddenItems);
            for (EntryStack<?> stack : this.hiddenItems) {
                rule.hide(stack);
            }
        } else if(CommonConfig.debugMode.get()) LOGGER.warn("There are no items that are supposed to be hidden in REI");
    }

    private void add(EntryStack<?> entryStack) {
        this.hiddenItems.add(entryStack);
    }

    private void add(List<EntryStack<?>> entryStacks) {
        this.hiddenItems.addAll(entryStacks);
    }
}