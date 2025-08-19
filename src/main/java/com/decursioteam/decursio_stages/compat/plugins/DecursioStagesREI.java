package com.decursioteam.decursio_stages.compat.plugins;

import com.decursioteam.decursio_stages.Registry;
import com.decursioteam.decursio_stages.config.CommonConfig;
import com.decursioteam.decursio_stages.datagen.RestrictionsData;
import com.decursioteam.decursio_stages.datagen.utils.IStagesData;
import com.decursioteam.decursio_stages.events.SyncStagesEvent;
import com.decursioteam.decursio_stages.restrictions.FluidRestriction;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.client.event.RecipesUpdatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.decursioteam.decursio_stages.DecursioStages.LOGGER;
import static com.decursioteam.decursio_stages.utils.ResourceUtil.*;

@REIPluginClient
public class DecursioStagesREI implements REIClientPlugin {

    private final List<EntryStack<?>> hiddenEntries = new ArrayList<>();

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

        collectEntries(rule);
        collectEntries(entryRegistry);
        hideEntries(rule);
    }

    private void collectEntries(BasicFilteringRule<?> rule) {
        if (!this.hiddenEntries.isEmpty()) {
            if(CommonConfig.debugMode.get()) LOGGER.info("Restoring the following items at runtime: " + this.hiddenEntries);
            for (EntryStack<?> stack : this.hiddenEntries) {
                rule.show(stack);
            }
            this.hiddenEntries.clear();
        } else if(CommonConfig.debugMode.get()) LOGGER.warn("There are no items available for restoring to the REI ingredient list!");
    }

    private void collectEntries(EntryRegistry registry) {
        registry.getEntryStacks().forEach(entryStack -> {
            final Player player = Minecraft.getInstance().player;
            final IStagesData stageData = StageUtil.getPlayerData(player);
            try {
                Registry.getRestrictionsHashSet().forEach((restriction) -> {
                    String stage = RestrictionsData.getRestrictionData(restriction).getData().getStage();
                    assert stageData != null;
                    if (!stageData.hasStage(stage) && RestrictionsData.getRestrictionData(restriction).getSettingsCodec().getHideInJEI()) {
                        if(ForgeRegistries.ITEMS.getDelegate(entryStack.getIdentifier()).isPresent())
                        {
                            ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getDelegate(entryStack.getIdentifier()).get());
                            Fluid fluid = ForgeRegistries.FLUIDS.getValue(entryStack.getIdentifier());
                            if (!getFluids(restriction).isEmpty() && fluid != null) {
                                for (FluidRestriction fluidRestriction : getFluids(restriction)) {
                                    if(fluidRestriction.getFluid() != null) {
                                        if (fluid.isSame(fluidRestriction.getFluid())) {
                                            add(entryStack);
                                        }
                                    }
                                    else if(fluidRestriction.getTag() != null) {
                                        if (fluid.is(Objects.requireNonNull(ForgeRegistries.FLUIDS.tags()).createTagKey(fluidRestriction.getTag()))) {
                                            add(entryStack);
                                        }
                                    }
                                    else if(fluidRestriction.getMod() != null) {
                                        if (Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluid)).getNamespace().equals(fluidRestriction.getMod())) {
                                            add(entryStack);
                                        }
                                    }
                                }
                            }
                            if (!getMods(restriction).isEmpty()) {
                                if(check(restriction, stack, ResourceUtil.CHECK_TYPES.MOD)) {
                                    add(entryStack);
                                }
                            }
                            if(!getTags(restriction).isEmpty()) {
                                if(check(restriction, stack, CHECK_TYPES.TAG)) {
                                    add(entryStack);
                                }
                            }
                            if (!getItems(restriction).isEmpty()) {
                                if (check(restriction, stack, CHECK_TYPES.ITEM)) {
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

    private void hideEntries(BasicFilteringRule<?> rule) {
        if (!this.hiddenEntries.isEmpty()) {
            if(CommonConfig.debugMode.get()) LOGGER.info("Hiding the following items: " + this.hiddenEntries);
            for (EntryStack<?> stack : this.hiddenEntries) {
                rule.hide(stack);
            }
        } else if(CommonConfig.debugMode.get()) LOGGER.warn("There are no items that are supposed to be hidden in REI");
    }

    private void add(EntryStack<?> entryStack) {
        this.hiddenEntries.add(entryStack);
    }

    private void add(List<EntryStack<?>> entryStacks) {
        this.hiddenEntries.addAll(entryStacks);
    }
}