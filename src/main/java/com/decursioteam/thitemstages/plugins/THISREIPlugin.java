package com.decursioteam.thitemstages.plugins;

import com.decursioteam.thitemstages.Registry;
import com.decursioteam.thitemstages.datagen.RestrictionsData;
import com.decursioteam.thitemstages.datagen.utils.IStagesData;
import com.decursioteam.thitemstages.utils.ResourceUtil;
import com.decursioteam.thitemstages.utils.StageUtil;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.common.entry.type.EntryTypeRegistry;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.forge.REIPluginClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Objects;

import static com.decursioteam.thitemstages.utils.ResourceUtil.*;

@REIPluginClient
public class THISREIPlugin implements REIClientPlugin {
    @Override
    public void registerEntryTypes(EntryTypeRegistry registry)
    {
        final Player player = Minecraft.getInstance().player;
        final IStagesData stageData = StageUtil.getPlayerData(player);
        try {
            Registry.getRestrictionsHashSet().forEach((s) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                assert stageData != null;
                if (!stageData.hasStage(stage) && RestrictionsData.getRestrictionData(s).getSettingsCodec().getHideInJEI()) {
                    if(registry.keySet().isEmpty())
                        return;
                    registry.keySet().forEach(resourceLocation -> {
                        if(Objects.requireNonNull(registry.get(resourceLocation)).getType() != VanillaEntryTypes.ITEM)
                            return;
                        if(ForgeRegistries.ITEMS.getDelegate(resourceLocation).isPresent())
                        {
                            ItemStack stack = new ItemStack(ForgeRegistries.ITEMS.getDelegate(resourceLocation).get());
                            if (!getMods(s).isEmpty()) {
                                if(check(s, stack, ResourceUtil.CHECK_TYPES.MOD))
                                    registry.keySet().remove(resourceLocation);
                            }
                            if(!getTags(s).isEmpty()) {
                                if(check(s, stack, CHECK_TYPES.TAG))
                                    registry.keySet().remove(resourceLocation);
                            }
                            if (!getItems(s).isEmpty()) {
                                if (check(s, stack, CHECK_TYPES.ITEM)) {
                                    registry.keySet().remove(resourceLocation);
                                }
                            }
                        }
                    });
                }
            });
        }
        catch (NullPointerException e){
            //
        }
    }

}
