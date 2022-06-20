package com.decursioteam.thitemstages.events;

import com.decursioteam.thitemstages.Registry;
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
import java.util.Set;

import static com.decursioteam.thitemstages.utils.ResourceUtil.*;
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
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                if(!hasStage(player, stage)){

                    if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist()){
                        if(getContainers(s).contains(containerName)) {
                            for (ItemStack item : event.getPlayer().inventoryMenu.getItems()) {
                                if(checkAllItems(s, item)) {
                                    player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                    InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                                }
                            }
                        }
                    }
                    if(RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist()){
                        if(!getContainers(s).contains(containerName)) {
                            for (ItemStack item : event.getPlayer().inventoryMenu.getItems()) {
                                if(checkAllItems(s, item)) {
                                    player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                    InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
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
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                if(!RestrictionsData.getRestrictionData(s).getData().getDimensionList().isEmpty() && !hasStage(player, stage)){

                    if(getDimensions(s).contains(event.getDimension().location())) {
                        player.displayClientMessage(new StringTextComponent("You don't have access to this dimension!").withStyle(TextFormatting.RED), true);
                        event.setCanceled(true);
                    }
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void clientInventoryTick(TickEvent.PlayerTickEvent event){
        if(event.side.isClient() || event.player instanceof FakePlayer) return;
        PlayerEntity player = event.player;

        if(!new HashSet<>(player.inventory.items).equals(prevInventory) || !player.inventoryMenu.getCraftSlots().isEmpty()){
            Registry.getRestrictions().forEach((s, entityType) -> {

                String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                if(!hasStage(player, stage) && RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist()) {
                    if(!getContainers(s).contains("thitemstages.inventoryMenu.CraftingGrid")){
                        for (int i = 0; i < player.inventoryMenu.getCraftSlots().getContainerSize(); i++) {
                            ItemStack item = player.inventoryMenu.getCraftSlots().getItem(i);
                            if(checkAllItems(s, item)) {
                                player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                            }
                        }
                    }
                }
                if(!hasStage(player, stage) && !RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist()) {
                    if (getContainers(s).contains("thitemstages.inventoryMenu.CraftingGrid")) {
                        for (int i = 0; i < player.inventoryMenu.getCraftSlots().getContainerSize(); i++) {
                            ItemStack item = player.inventoryMenu.getCraftSlots().getItem(i);
                            if(checkAllItems(s, item)) {
                                player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                                InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                            }
                        }
                    }
                }
                if(RestrictionsData.getRestrictionData(s).getSettingsCodec().getCheckPlayerInventory() && !hasStage(player, stage)){
                    for (ItemStack item : player.inventory.items) {
                        //Drop ingredients from mod list
                        if (getExceptions(s).contains(item.getItem().getRegistryName())) return;
                        if(checkAllItems(s, item)) {
                            player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                            InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
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
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                if(RestrictionsData.getRestrictionData(s).getSettingsCodec().getCheckPlayerEquipment() && !hasStage(player, stage)){
                    for (ItemStack item : player.inventory.armor) {
                        if(checkAllItems(s, item)) {
                            player.displayClientMessage(new StringTextComponent("Unavailable items were dropped from your inventory!").withStyle(TextFormatting.RED), true);
                            InventoryHelper.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
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
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                if(!hasStage(player, stage) && player.containerMenu != player.inventoryMenu) {
                    if (!RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist()) {
                        if (getContainers(s).contains(player.containerMenu.getClass().getName())) {
                            if(checkAllItems(s, item)) {
                                event.setCanceled(true);
                                event.getItem().setPickUpDelay(RestrictionsData.getRestrictionData(s).getSettingsCodec().getPickupDelay());
                            }
                        }
                    }
                    if (RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist()) {
                        if (!getContainers(s).contains(player.containerMenu.getClass().getName())) {
                            if(checkAllItems(s, item)) {
                                event.setCanceled(true);
                                event.getItem().setPickUpDelay(RestrictionsData.getRestrictionData(s).getSettingsCodec().getPickupDelay());
                            }
                        }
                    }
                }
                if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getCanPickup() && !hasStage(player, stage)){
                    if(checkAllItems(s, item)) {
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
            String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
            if(RestrictionsData.getRestrictionData(s).getSettingsCodec().getAdvancedTooltips().equals("NONE")) return;
            if ((RestrictionsData.getRestrictionData(s).getSettingsCodec().getAdvancedTooltips().equals("ADVANCED") && !playerStages.contains(stage) && event.getFlags().isAdvanced()) || !playerStages.contains(stage) && RestrictionsData.getRestrictionData(s).getSettingsCodec().getAdvancedTooltips().equals("ALWAYS")) {
                ItemStack itemStack = null;
                //Collect ingredients from mod list
                if(getExceptions(s).contains(event.getItemStack().getItem().getRegistryName())) return;

                if(checkAllItems(s, event.getItemStack())) {
                    itemStack = event.getItemStack();
                }

                if(itemStack != null){
                    if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getItemTitle().equals("")) {
                        event.getToolTip().set(0, new StringTextComponent(RestrictionsData.getRestrictionData(s).getSettingsCodec().getItemTitle()));
                    }
                    event.getToolTip().add(new TranslationTextComponent("thitemstages.tooltip.stage.message").withStyle(TextFormatting.DARK_PURPLE).withStyle(TextFormatting.BOLD)
                            .append(new TranslationTextComponent("thitemstages.tooltip.stage.left_bracket").withStyle(TextFormatting.BLUE).withStyle(TextFormatting.DARK_PURPLE))
                            .append(new TranslationTextComponent("thitemstages.tooltip.stage.stage", stage).withStyle(TextFormatting.WHITE).withStyle(TextFormatting.BOLD))
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
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableBlocks() && !hasStage(player, stage)){
                    if(getBlocks(s).contains(event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName())){
                        event.setCanceled(true);
                        player.displayClientMessage(new StringTextComponent("You can't interact with this block!").withStyle(TextFormatting.RED), true);
                    }

                    if(!getTags(s).isEmpty()){
                        for (ResourceLocation tagID : getTags(s)) {
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
    public void onPlayerInteractWithBlock(PlayerInteractEvent.LeftClickBlock event){
        if(event.getEntityLiving() instanceof PlayerEntity){
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            Registry.getRestrictions().forEach((s, x) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableBlocks() && !hasStage(player, stage)){
                    if(checkAllItems(s, new ItemStack(event.getWorld().getBlockState(event.getPos()).getBlock().asItem()))) {
                        player.displayClientMessage(new StringTextComponent("You won't be able to use this block!").withStyle(TextFormatting.RED), true);
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
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableItems() && !hasStage(player, stage)){
                    if (getExceptions(s).contains(player.getMainHandItem().getItem().getRegistryName())) return;
                    if(checkAllItems(s, player.getMainHandItem())) {
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
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableItems() && !hasStage(player, stage)){
                    if(checkAllItems(s, event.getItemStack())) {
                        event.setCanceled(true);
                        player.displayClientMessage(new StringTextComponent("You can't interact with this item!").withStyle(TextFormatting.RED), true);
                    }
                }
            });
        }
    }
}
