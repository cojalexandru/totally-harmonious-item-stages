package com.decursioteam.decursio_stages.events.ore_staging;

import com.decursioteam.decursio_stages.DecursioStages;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.event.entity.item.ItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;
@Mod.EventBusSubscriber(modid = DecursioStages.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModelBakeEventHandler {

    private static final ResourceLocation IRON_ORE_MODEL = new ModelResourceLocation("minecraft","iron_ore", "");
    private static final ResourceLocation STONE_MODEL = new ModelResourceLocation("minecraft", "stone", "");

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void onModelBake(ModelEvent.ModifyBakingResult event) {
        Minecraft mc = Minecraft.getInstance();
        if(mc.level != null && mc.player != null){
            if(mc.player.isCreative() )
            {
                Map<ResourceLocation, BakedModel> modelRegistry = event.getModels();
                DecursioStages.LOGGER.info(event.getModels().keySet());

                BakedModel ironOreModel = modelRegistry.get(IRON_ORE_MODEL);
                BakedModel stoneModel = modelRegistry.get(STONE_MODEL);

                if (ironOreModel != null && stoneModel != null) {

                    DynamicBlockModel dynamicModel = new DynamicBlockModel(ironOreModel, stoneModel);
                    modelRegistry.put(IRON_ORE_MODEL, dynamicModel);
                }
            }
        }
    }
}