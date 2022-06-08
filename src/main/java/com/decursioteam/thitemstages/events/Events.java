package com.decursioteam.thitemstages.events;

import com.decursioteam.thitemstages.Registry;
import com.decursioteam.thitemstages.THItemStages;
import com.decursioteam.thitemstages.config.CommonConfig;
import com.decursioteam.thitemstages.datagen.RestrictionsData;
import com.decursioteam.thitemstages.datagen.utils.IStagesData;
import com.decursioteam.thitemstages.utils.StageUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static com.decursioteam.thitemstages.utils.StageUtil.hasStage;

public class Events {

    private Set<ItemStack> prevInventory;

    @SubscribeEvent
    public void playerContainerOpenEvent(PlayerContainerEvent.Open event) {
        String containerName = event.getContainer().getClass().getName();
        if(CommonConfig.debugMode.get()) event.getPlayer().displayClientMessage(new StringTextComponent(containerName), false);
        if(event.getEntity() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
            Registry.getRestrictions().forEach((s, entityType) -> {
                if(!hasStage(player, s)){
                    Set<String> containerList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getContainerList());
                    Set<ResourceLocation> itemList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getItemList());
                    Set<ResourceLocation> tagList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getTagList());
                    Set<ResourceLocation> blockList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getBlockList());
                    Set<String> modList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getModList());
                    Set<ResourceLocation> exceptionList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getExceptionList());
                    if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist()){
                        if(containerList.contains(containerName)) {
                            for (ItemStack item : event.getPlayer().inventoryMenu.getItems()) {
                                //Drop ingredients from mod list
                                if(!modList.isEmpty()){
                                    for (String modID : modList) {
                                        if (Objects.requireNonNull(item.getItem().getRegistryName()).getNamespace().equals(modID) && !exceptionList.contains(item.getItem().getRegistryName())) {
                                            InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX() + 0.5, player.getY(), player.getZ() + 0.5, item);
                                            player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                        }
                                    }
                                }
                                if(!tagList.isEmpty()){
                                    for (ResourceLocation tagID : tagList) {
                                        if (item.getItem().getTags().contains(tagID) && !exceptionList.contains(item.getItem().getRegistryName())) {
                                            InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX() + 0.5, player.getY(), player.getZ() + 0.5, item);
                                            player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                        }
                                    }
                                }

                                //Drop ingredients from item list
                                if(itemList.contains(item.getItem().getRegistryName())) {
                                    InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX() + 0.5, player.getY(), player.getZ() + 0.5, item);
                                    player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                }

                                //Drop ingredients from block list
                                if(blockList.contains(item.getItem().getRegistryName())) {
                                    InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX() + 0.5, player.getY(), player.getZ() + 0.5, item);
                                    player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                }
                            }
                        }
                    }
                    if(RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist()){
                        if(!containerList.contains(containerName)) {
                            for (ItemStack item : event.getPlayer().inventoryMenu.getItems()) {
                                //Drop ingredients from mod list
                                if(!modList.isEmpty()){
                                    for (String modID : modList) {
                                        if (Objects.requireNonNull(item.getItem().getRegistryName()).getNamespace().equals(modID) && !exceptionList.contains(item.getItem().getRegistryName())) {
                                            InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX() + 0.5, player.getY() + 0.5, player.getZ() + 0.5, item);
                                            item.setCount(0);
                                            player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                        }
                                    }
                                }
                                if(!tagList.isEmpty()){
                                    for (ResourceLocation tagID : tagList) {
                                        if (item.getItem().getTags().contains(tagID) && !exceptionList.contains(item.getItem().getRegistryName())) {
                                            InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX() + 0.5, player.getY(), player.getZ() + 0.5, item);
                                            player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                        }
                                    }
                                }

                                //Drop ingredients from item list
                                if(itemList.contains(item.getItem().getRegistryName())) {
                                    InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX() + 0.5, player.getY(), player.getZ() + 0.5, item);
                                    player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                }

                                //Drop ingredients from block list
                                if(blockList.contains(item.getItem().getRegistryName())) {
                                    InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX() + 0.5, player.getY(), player.getZ() + 0.5, item);
                                    player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void entityTravelToDimension(EntityTravelToDimensionEvent event) {
        if(event.getEntity() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
            Registry.getRestrictions().forEach((s, entityType) -> {
                if(!RestrictionsData.getRestrictionData(s).getData().getDimensionList().isEmpty() && !hasStage(player, s)){

                    Set<ResourceLocation> dimensionList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getDimensionList());

                    if(dimensionList.contains(event.getDimension().location())) {
                        player.displayClientMessage(new StringTextComponent("You don't have access to this dimension!").withStyle(TextFormatting.RED), true);
                        event.setCanceled(true);
                    }
                }
            });
        }
    }

    private static boolean areCraftingSlotsEmpty(PlayerEntity player) {
        for (int i = 0; i < player.inventoryMenu.getCraftSlots().getContainerSize(); i++) {
            if(player.inventoryMenu.getCraftSlots().getItem(i).isEmpty()) return true;
        }
        return false;
    }


    @SubscribeEvent(priority = EventPriority.LOW)
    public void clientInventoryTick(TickEvent.PlayerTickEvent event){
        if(event.side.isClient() || event.player instanceof FakePlayer) return;
        PlayerEntity player = event.player;
        if(!new HashSet<>(player.inventory.items).equals(prevInventory) || areCraftingSlotsEmpty(player)){
            Registry.getRestrictions().forEach((s, entityType) -> {
                Set<String> containerList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getContainerList());
                Set<ResourceLocation> itemList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getItemList());
                Set<ResourceLocation> tagList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getTagList());
                Set<ResourceLocation> blockList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getBlockList());
                Set<String> modList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getModList());
                Set<ResourceLocation> exceptionList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getExceptionList());
                exceptionList.add(new ResourceLocation("minecraft:air"));

                if(!hasStage(player, s) && RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist()) {
                    if(!containerList.contains("thitemstages.inventoryMenu.CraftingGrid")){
                        for (int i = 0; i < player.inventoryMenu.getCraftSlots().getContainerSize(); i++) {
                            ItemStack item = player.inventoryMenu.getCraftSlots().getItem(i);
                            if (exceptionList.contains(item.getItem().getRegistryName())) return;
                            if(!modList.isEmpty()){
                                for (String modID : modList) {
                                    if (Objects.requireNonNull(item.getItem().getRegistryName()).getNamespace().equals(modID)) {
                                        player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                        InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                                    }
                                }
                            }

                            //Drop ingredients from item list
                            if(itemList.contains(item.getItem().getRegistryName())) {
                                player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                            }

                            //Drop ingredients from block list
                            if(blockList.contains(item.getItem().getRegistryName())) {
                                player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                            }

                            if(!tagList.isEmpty()){
                                for (ResourceLocation tagID : tagList) {
                                    if (item.getItem().getTags().contains(tagID)) {
                                        InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX() + 0.5, player.getY(), player.getZ() + 0.5, item);
                                        player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                    }
                                }
                            }
                        }
                    }
                    if(!hasStage(player, s) && !RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist()) {
                        if (containerList.contains("thitemstages.inventoryMenu.CraftingGrid")) {
                            for (int i = 0; i < player.inventoryMenu.getCraftSlots().getContainerSize(); i++) {
                                ItemStack item = player.inventoryMenu.getCraftSlots().getItem(i);
                                if (exceptionList.contains(item.getItem().getRegistryName())) return;
                                if (!modList.isEmpty()) {
                                    for (String modID : modList) {
                                        if (Objects.requireNonNull(item.getItem().getRegistryName()).getNamespace().equals(modID)) {
                                            player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                            InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                                        }
                                    }
                                }

                                //Drop ingredients from item list
                                if (itemList.contains(item.getItem().getRegistryName())) {
                                    player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                    InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                                }

                                //Drop ingredients from block list
                                if (blockList.contains(item.getItem().getRegistryName())) {
                                    player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                    InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                                }

                                if (!tagList.isEmpty()) {
                                    for (ResourceLocation tagID : tagList) {
                                        if (item.getItem().getTags().contains(tagID)) {
                                            InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX() + 0.5, player.getY(), player.getZ() + 0.5, item);
                                            player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if(RestrictionsData.getRestrictionData(s).getSettingsCodec().getCheckPlayerInventory() && !hasStage(player, s)){
                    for (ItemStack item : player.inventory.items) {
                        //Drop ingredients from mod list
                        if (exceptionList.contains(item.getItem().getRegistryName())) return;
                        if(!modList.isEmpty()){
                            for (String modID : modList) {
                                if (Objects.requireNonNull(item.getItem().getRegistryName()).getNamespace().equals(modID)) {
                                    player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory! 1").withStyle(TextFormatting.RED), true);
                                    InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                                }
                            }
                        }

                        //Drop ingredients from item list
                        if(itemList.contains(item.getItem().getRegistryName())) {
                            player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                            InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                        }

                        //Drop ingredients from block list
                        if(blockList.contains(item.getItem().getRegistryName())) {
                            player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                            InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                        }

                        if(!tagList.isEmpty()){
                            for (ResourceLocation tagID : tagList) {
                                if (item.getItem().getTags().contains(tagID)) {
                                    InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX() + 0.5, player.getY(), player.getZ() + 0.5, item);
                                    player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                }
                            }
                        }
                    }
                }
            });
            prevInventory = new HashSet<>(player.inventory.items);
        }
    }

    @SubscribeEvent
    public void onEquipmentChange(LivingEquipmentChangeEvent event){
        if(event.getEntityLiving().getCommandSenderWorld().isClientSide()) return;
        if(event.getEntityLiving() instanceof PlayerEntity){
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            Registry.getRestrictions().forEach((s, entityType) -> {
                if(RestrictionsData.getRestrictionData(s).getSettingsCodec().getCheckPlayerEquipment() && !hasStage(player, s)){

                    Set<ResourceLocation> itemList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getItemList());
                    Set<ResourceLocation> tagList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getTagList());
                    Set<ResourceLocation> blockList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getBlockList());
                    Set<String> modList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getModList());
                    Set<ResourceLocation> exceptionList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getExceptionList());
                    exceptionList.add(new ResourceLocation("minecraft:air"));

                    for (ItemStack item : player.inventory.armor) {
                        //Drop ingredients from mod list
                        if(!modList.isEmpty()){
                            for (String modID : modList) {
                                if (exceptionList.contains(item.getItem().getRegistryName())) return;
                                if (Objects.requireNonNull(item.getItem().getRegistryName()).getNamespace().equals(modID)) {
                                    player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                    InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                                }
                            }
                        }

                        //Drop ingredients from block list
                        if(blockList.contains(item.getItem().getRegistryName())) {
                            player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                            InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                        }

                        //Drop ingredients from item list
                        if(itemList.contains(item.getItem().getRegistryName())) {
                            player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                            InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                        }

                        if(!tagList.isEmpty()){
                            for (ResourceLocation tagID : tagList) {
                                if (item.getItem().getTags().contains(tagID)) {
                                    InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX() + 0.5, player.getY(), player.getZ() + 0.5, item);
                                    player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                }
                            }
                        }
                    }
                }
            });
        }
    }


    @SubscribeEvent
    public void onItemPickupEvent(EntityItemPickupEvent event){
        if(event.getEntityLiving() instanceof PlayerEntity){
            ServerPlayerEntity player = (ServerPlayerEntity) event.getEntityLiving();
            ItemStack item = event.getItem().getItem();
            Registry.getRestrictions().forEach((s, entityType) -> {
                Set<String> containerList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getContainerList());
                Set<ResourceLocation> itemList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getItemList());
                Set<ResourceLocation> blockList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getBlockList());
                Set<ResourceLocation> tagList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getTagList());
                Set<String> modList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getModList());
                Set<ResourceLocation> exceptionList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getExceptionList());
                exceptionList.add(new ResourceLocation("minecraft:air"));
                if(!hasStage(player, s) && player.containerMenu != player.inventoryMenu) {
                    if (!RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist()) {
                        if (containerList.contains(player.containerMenu.getClass().getName())) {
                            //Drop ingredients from mod list
                            if (!modList.isEmpty()) {
                                if (exceptionList.contains(item.getItem().getRegistryName())) return;
                                for (String modID : modList) {
                                    if (Objects.requireNonNull(item.getItem().getRegistryName()).getNamespace().equals(modID)) {
                                        event.setCanceled(true);
                                        event.getItem().setPickUpDelay(RestrictionsData.getRestrictionData(s).getSettingsCodec().getPickupDelay());
                                    }
                                }
                            }

                            if(!tagList.isEmpty()){
                                for (ResourceLocation tagID : tagList) {
                                    if (item.getItem().getTags().contains(tagID)) {
                                        event.setCanceled(true);
                                        event.getItem().setPickUpDelay(RestrictionsData.getRestrictionData(s).getSettingsCodec().getPickupDelay());;
                                    }
                                }
                            }

                            //Drop ingredients from block list
                            if (blockList.contains(item.getItem().getRegistryName())) {
                                event.setCanceled(true);
                                event.getItem().setPickUpDelay(RestrictionsData.getRestrictionData(s).getSettingsCodec().getPickupDelay());
                            }

                            //Drop ingredients from item list
                            if (itemList.contains(item.getItem().getRegistryName())) {
                                event.setCanceled(true);
                                event.getItem().setPickUpDelay(RestrictionsData.getRestrictionData(s).getSettingsCodec().getPickupDelay());
                            }
                        }
                    }
                    if (RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist()) {
                        if (!containerList.contains(player.containerMenu.getClass().getName())) {
                            //Drop ingredients from mod list
                            if (!modList.isEmpty()) {
                                if (exceptionList.contains(item.getItem().getRegistryName())) return;
                                for (String modID : modList) {
                                    if (Objects.requireNonNull(item.getItem().getRegistryName()).getNamespace().equals(modID)) {
                                        event.setCanceled(true);
                                        event.getItem().setPickUpDelay(RestrictionsData.getRestrictionData(s).getSettingsCodec().getPickupDelay());;
                                    }
                                }
                            }

                            if(!tagList.isEmpty()){
                                for (ResourceLocation tagID : tagList) {
                                    if (item.getItem().getTags().contains(tagID)) {
                                        event.setCanceled(true);
                                        event.getItem().setPickUpDelay(RestrictionsData.getRestrictionData(s).getSettingsCodec().getPickupDelay());;
                                    }
                                }
                            }

                            //Drop ingredients from block list
                            if (blockList.contains(item.getItem().getRegistryName())) {
                                event.setCanceled(true);
                                event.getItem().setPickUpDelay(RestrictionsData.getRestrictionData(s).getSettingsCodec().getPickupDelay());
                            }

                            //Drop ingredients from item list
                            if (itemList.contains(item.getItem().getRegistryName())) {
                                event.setCanceled(true);
                                event.getItem().setPickUpDelay(RestrictionsData.getRestrictionData(s).getSettingsCodec().getPickupDelay());
                            }
                        }
                    }
                }
                if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getCanPickup() && !hasStage(player, s)){
                    //Drop ingredients from mod list
                    if(!modList.isEmpty()){
                        if (exceptionList.contains(item.getItem().getRegistryName())) return;
                        for (String modID : modList) {
                            if (Objects.requireNonNull(item.getItem().getRegistryName()).getNamespace().equals(modID)) {
                                event.setCanceled(true);
                                event.getItem().setPickUpDelay(RestrictionsData.getRestrictionData(s).getSettingsCodec().getPickupDelay());
                                player.displayClientMessage(new StringTextComponent("You can't pick up this item!").withStyle(TextFormatting.RED), true);
                            }
                        }
                    }

                    //Drop ingredients from block list
                    if(blockList.contains(item.getItem().getRegistryName())) {
                        event.setCanceled(true);
                        event.getItem().setPickUpDelay(RestrictionsData.getRestrictionData(s).getSettingsCodec().getPickupDelay());
                        player.displayClientMessage(new StringTextComponent("You can't pick up this item!").withStyle(TextFormatting.RED), true);
                    }

                    //Drop ingredients from item list
                    if(itemList.contains(item.getItem().getRegistryName())) {
                        event.setCanceled(true);
                        event.getItem().setPickUpDelay(RestrictionsData.getRestrictionData(s).getSettingsCodec().getPickupDelay());
                        player.displayClientMessage(new StringTextComponent("You can't pick up this item!").withStyle(TextFormatting.RED), true);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onItemTooltip(ItemTooltipEvent event){
        PlayerEntity player = Minecraft.getInstance().player;
        final IStagesData stageData = StageUtil.getPlayerData(player);
        if (stageData == null) return;
        final ArrayList<String> playerStages = stageData.getStages();
        Registry.getRestrictions().forEach((s, x) -> {
            if(RestrictionsData.getRestrictionData(s).getSettingsCodec().getAdvancedTooltips().equals("NONE")) return;
            if ((RestrictionsData.getRestrictionData(s).getSettingsCodec().getAdvancedTooltips().equals("ADVANCED") && !playerStages.contains(s) && event.getFlags().isAdvanced()) || !playerStages.contains(s) && RestrictionsData.getRestrictionData(s).getSettingsCodec().getAdvancedTooltips().equals("ALWAYS")) {
                Set<ResourceLocation> itemList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getItemList());
                Set<ResourceLocation> blockList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getBlockList());
                Set<ResourceLocation> tagList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getTagList());
                Set<String> modList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getModList());
                Set<ResourceLocation> exceptionList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getExceptionList());
                exceptionList.add(new ResourceLocation("minecraft:air"));
                ItemStack itemStack = null;
                //Collect ingredients from mod list
                if(exceptionList.contains(event.getItemStack().getItem().getRegistryName())) return;
                if(!modList.isEmpty()) {
                    for (String modID : modList) {
                        if (Objects.requireNonNull(event.getItemStack().getItem().getRegistryName()).getNamespace().contains(modID)) {
                            itemStack = event.getItemStack();
                        }
                    }
                }

                if(!tagList.isEmpty()) {
                    for (ResourceLocation tagID : tagList) {
                        if (event.getItemStack().getItem().getTags().contains(tagID)) {
                            itemStack = event.getItemStack();
                        }
                    }
                }

                //Collect ingredients from item list
                if (itemList.contains(event.getItemStack().getItem().getRegistryName())) {
                    itemStack = event.getItemStack();
                }

                //Collect ingredients from block list
                if (blockList.contains(event.getItemStack().getItem().getRegistryName())) {
                    itemStack = event.getItemStack();
                }
                if(itemStack != null){
                    if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getItemTitle().equals("")) {
                        event.getToolTip().set(0, new StringTextComponent(RestrictionsData.getRestrictionData(s).getSettingsCodec().getItemTitle()));
                    }
                    event.getToolTip().add(new TranslationTextComponent("thitemstages.tooltip.stage.message").withStyle(TextFormatting.DARK_PURPLE).withStyle(TextFormatting.BOLD)
                            .append(new TranslationTextComponent("thitemstages.tooltip.stage.left_bracket").withStyle(TextFormatting.BLUE).withStyle(TextFormatting.DARK_PURPLE))
                            .append(new TranslationTextComponent("thitemstages.tooltip.stage.stage", s).withStyle(TextFormatting.WHITE).withStyle(TextFormatting.BOLD))
                            .append(new TranslationTextComponent("thitemstages.tooltip.stage.right_bracket").withStyle(TextFormatting.BLUE).withStyle(TextFormatting.DARK_PURPLE))
                    );
                    if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getCanPickup()){
                        event.getToolTip().add(new TranslationTextComponent("thitemstages.tooltip.pickup").withStyle(TextFormatting.WHITE));
                    }
                    if(RestrictionsData.getRestrictionData(s).getSettingsCodec().getCheckPlayerInventory()){
                        event.getToolTip().add(new TranslationTextComponent("thitemstages.tooltip.playerinventory").withStyle(TextFormatting.WHITE));
                    }
                    if(RestrictionsData.getRestrictionData(s).getSettingsCodec().getCheckPlayerEquipment()){
                        event.getToolTip().add(new TranslationTextComponent("thitemstages.tooltip.playerequipment").withStyle(TextFormatting.WHITE));
                    }
                    if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableItems()){
                        event.getToolTip().add(new TranslationTextComponent("thitemstages.tooltip.usableitems").withStyle(TextFormatting.WHITE));
                    }
                    if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableBlocks()){
                        event.getToolTip().add(new TranslationTextComponent("thitemstages.tooltip.usableblocks").withStyle(TextFormatting.WHITE));
                    }
                    if(RestrictionsData.getRestrictionData(s).getSettingsCodec().getHideInJEI()){
                        event.getToolTip().add(new TranslationTextComponent("thitemstages.tooltip.hideinjei").withStyle(TextFormatting.WHITE));
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public void onPlayerInteractWithBlock(PlayerInteractEvent.RightClickBlock event){
        if(event.getEntityLiving() instanceof PlayerEntity){
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            Registry.getRestrictions().forEach((s, x) -> {
                if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableBlocks() && !hasStage(player, s)){

                    Set<ResourceLocation> blockList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getBlockList());
                    Set<ResourceLocation> tagList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getTagList());

                    if(blockList.contains(event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName())){
                        event.setCanceled(true);
                        player.displayClientMessage(new StringTextComponent("You can't interact with this block!").withStyle(TextFormatting.RED), true);
                    }

                    if(!tagList.isEmpty()){
                        for (ResourceLocation tagID : tagList) {
                            if (event.getWorld().getBlockState(event.getPos()).getBlock().getTags().contains(tagID)) {
                                event.setCanceled(true);
                                player.displayClientMessage(new StringTextComponent("You can't interact with this block!").withStyle(TextFormatting.RED), true);
                            }
                        }
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(LivingAttackEvent event){
        if(event.getSource() != null && event.getSource().getEntity() instanceof PlayerEntity && event.isCancelable() && event.getSource().getEntity() != null && !event.getSource().getEntity().level.isClientSide && !(event.getSource().getEntity() instanceof FakePlayer)){
            PlayerEntity player = (PlayerEntity) event.getSource().getEntity();
            Registry.getRestrictions().forEach((s, x) -> {
                if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableItems() && !hasStage(player, s)){

                    Set<ResourceLocation> itemList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getItemList());
                    Set<ResourceLocation> tagList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getTagList());
                    Set<ResourceLocation> blockList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getBlockList());
                    Set<String> modList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getModList());
                    Set<ResourceLocation> exceptionList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getExceptionList());
                    exceptionList.add(new ResourceLocation("minecraft:air"));

                    //Drop ingredients from mod list
                    if (exceptionList.contains(player.getMainHandItem().getItem().getRegistryName())) return;
                    if(!modList.isEmpty()){
                        for (String modID : modList) {
                            if (Objects.requireNonNull(player.getMainHandItem().getItem().getRegistryName()).getNamespace().equals(modID)) {
                                event.setCanceled(true);
                                player.displayClientMessage(new StringTextComponent("You can't interact with this item!").withStyle(TextFormatting.RED), true);
                            }
                        }
                    }

                    if(!tagList.isEmpty()){
                        for (ResourceLocation tagID : tagList) {
                            if (player.getMainHandItem().getItem().getTags().contains(tagID)) {
                                event.setCanceled(true);
                                player.displayClientMessage(new StringTextComponent("You can't interact with this item!").withStyle(TextFormatting.RED), true);
                            }
                        }
                    }

                    //Drop ingredients from block list
                    if(blockList.contains(player.getMainHandItem().getItem().getRegistryName())) {
                        event.setCanceled(true);
                        player.displayClientMessage(new StringTextComponent("You can't interact with this item!").withStyle(TextFormatting.RED), true);
                    }
                    if(itemList.contains(player.getMainHandItem().getItem().getRegistryName())){
                        event.setCanceled(true);
                        player.displayClientMessage(new StringTextComponent("You can't interact with this item!").withStyle(TextFormatting.RED), true);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event){
        if(event.isCancelable() && event.getPlayer() != null && !event.getPlayer().level.isClientSide && !(event.getPlayer() instanceof FakePlayer)){
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            Registry.getRestrictions().forEach((s, x) -> {
                if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableItems() && !hasStage(player, s)){

                    Set<ResourceLocation> itemList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getItemList());
                    Set<ResourceLocation> blockList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getBlockList());
                    Set<ResourceLocation> tagList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getTagList());
                    Set<String> modList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getModList());
                    Set<ResourceLocation> exceptionList = new HashSet<>(RestrictionsData.getRestrictionData(s).getData().getExceptionList());
                    exceptionList.add(new ResourceLocation("minecraft:air"));

                    //Drop ingredients from mod list
                    if(!modList.isEmpty()){
                        if (exceptionList.contains(event.getItemStack().getItem().getRegistryName())) return;
                        for (String modID : modList) {
                            if (Objects.requireNonNull(event.getItemStack().getItem().getRegistryName()).getNamespace().equals(modID)) {
                                event.setCanceled(true);
                                player.displayClientMessage(new StringTextComponent("You can't interact with this item!").withStyle(TextFormatting.RED), true);
                            }
                        }
                    }

                    if(!tagList.isEmpty()){
                        for (ResourceLocation tagID : tagList) {
                            if (event.getItemStack().getItem().getTags().contains(tagID)) {
                                event.setCanceled(true);
                                player.displayClientMessage(new StringTextComponent("You can't interact with this item!").withStyle(TextFormatting.RED), true);
                            }
                        }
                    }

                    //Drop ingredients from block list
                    if(blockList.contains(event.getItemStack().getItem().getRegistryName())) {
                        event.setCanceled(true);
                        player.displayClientMessage(new StringTextComponent("You can't interact with this item!").withStyle(TextFormatting.RED), true);
                    }
                    if(itemList.contains(event.getItemStack().getItem().getRegistryName())){
                        event.setCanceled(true);
                        player.displayClientMessage(new StringTextComponent("You can't interact with this item!").withStyle(TextFormatting.RED), true);
                    }
                }
            });
        }
    }
}
