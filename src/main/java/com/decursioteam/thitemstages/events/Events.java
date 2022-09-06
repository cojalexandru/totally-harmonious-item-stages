package com.decursioteam.thitemstages.events;

import com.decursioteam.thitemstages.Registry;
import com.decursioteam.thitemstages.config.CommonConfig;
import com.decursioteam.thitemstages.datagen.RestrictionsData;
import com.decursioteam.thitemstages.datagen.utils.IStagesData;
import com.decursioteam.thitemstages.utils.StageUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraftforge.event.world.BlockEvent;
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
        if(CommonConfig.debugMode.get()) event.getPlayer().displayClientMessage(new TextComponent(containerName), false);

        if(event.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.getEntity();

            Registry.getRestrictions().forEach((s, entityType) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                if(!hasStage(player, stage)){

                    if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist()){
                        if(getContainers(s).contains(containerName)) {
                            for (ItemStack item : event.getPlayer().inventoryMenu.getItems()) {
                                if(checkAllItems(s, item)) {
                                    player.displayClientMessage(new TextComponent("Unavailable items were dropped from your inventory!").withStyle(ChatFormatting.RED), true);
                                    Containers.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                                }
                            }
                        }
                    }
                    if(RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist()){
                        if(!getContainers(s).contains(containerName)) {
                            for (ItemStack item : event.getPlayer().inventoryMenu.getItems()) {
                                if(checkAllItems(s, item)) {
                                    player.displayClientMessage(new TextComponent("Unavailable items were dropped from your inventory!").withStyle(ChatFormatting.RED), true);
                                    Containers.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
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
        if(event.getEntity() instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            Registry.getRestrictions().forEach((s, entityType) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                if(!RestrictionsData.getRestrictionData(s).getData().getDimensionList().isEmpty() && !hasStage(player, stage)){

                    if(getDimensions(s).contains(event.getDimension().location())) {
                        player.displayClientMessage(new TextComponent("You don't have access to this dimension!").withStyle(ChatFormatting.RED), true);
                        event.setCanceled(true);
                    }
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void clientInventoryTick(TickEvent.PlayerTickEvent event){
        if(event.side.isClient() || event.player instanceof FakePlayer) return;
        Player player = event.player;
        if(!new HashSet<>(player.inventoryMenu.getItems()).equals(prevInventory) || !player.inventoryMenu.getCraftSlots().isEmpty()){
            Registry.getRestrictions().forEach((s, entityType) -> {

                String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                if(!hasStage(player, stage) && RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist()) {
                    if(!getContainers(s).contains("thitemstages.inventoryMenu.CraftingGrid")){
                        for (int i = 0; i < player.inventoryMenu.getCraftSlots().getContainerSize(); i++) {
                            ItemStack item = player.inventoryMenu.getCraftSlots().getItem(i);
                            if(checkAllItems(s, item)) {
                                player.displayClientMessage(new TextComponent("Unavailable items were dropped from your inventory!").withStyle(ChatFormatting.RED), true);
                                Containers.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                            }
                        }
                    }
                }
                if(!hasStage(player, stage) && !RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist()) {
                    if (getContainers(s).contains("thitemstages.inventoryMenu.CraftingGrid")) {
                        for (int i = 0; i < player.inventoryMenu.getCraftSlots().getContainerSize(); i++) {
                            ItemStack item = player.inventoryMenu.getCraftSlots().getItem(i);
                            if(checkAllItems(s, item)) {
                                player.displayClientMessage(new TextComponent("Unavailable items were dropped from your inventory!").withStyle(ChatFormatting.RED), true);
                                Containers.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                            }
                        }
                    }
                }
                if(RestrictionsData.getRestrictionData(s).getSettingsCodec().getCheckPlayerInventory() && !hasStage(player, stage)){
                    for (ItemStack item : player.inventoryMenu.getItems()) {
                        //Drop ingredients from mod list
                        if (getExceptions(s).contains(item.getItem().getRegistryName())) return;
                        if(checkAllItems(s, item)) {
                            player.displayClientMessage(new TextComponent("Unavailable items were dropped from your inventory!").withStyle(ChatFormatting.RED), true);
                            Containers.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                        }
                    }
                }
            });
            prevInventory = new HashSet<>(player.inventoryMenu.getItems());
        }
    }

    @SubscribeEvent
    public void onEquipmentChange(LivingEquipmentChangeEvent event){
        if(event.getEntityLiving().getCommandSenderWorld().isClientSide()) return;
        if(event.getEntityLiving() instanceof Player){
            Player player = (Player) event.getEntityLiving();
            Registry.getRestrictions().forEach((s, entityType) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                if(RestrictionsData.getRestrictionData(s).getSettingsCodec().getCheckPlayerEquipment() && !hasStage(player, stage)){
                    for (ItemStack item : player.getArmorSlots()) {
                        if(checkAllItems(s, item)) {
                            player.displayClientMessage(new TextComponent("Unavailable items were dropped from your inventory!").withStyle(ChatFormatting.RED), true);
                            Containers.dropItemStack(player.getCommandSenderWorld(), player.getX(), player.getY(), player.getZ(), item);
                        }
                    }
                }
            });
        }
    }


    @SubscribeEvent
    public void onItemPickupEvent(EntityItemPickupEvent event){
        if(event.getEntityLiving() instanceof Player){
            ServerPlayer player = (ServerPlayer) event.getEntityLiving();
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
                        player.displayClientMessage(new TextComponent("You can't pick up this item!").withStyle(ChatFormatting.RED), true);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onItemTooltip(ItemTooltipEvent event){
        Player player = Minecraft.getInstance().player;
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
                        event.getToolTip().set(0, new TextComponent(RestrictionsData.getRestrictionData(s).getSettingsCodec().getItemTitle()));
                    }
                    event.getToolTip().add(new TranslatableComponent("thitemstages.tooltip.stage.message").withStyle(ChatFormatting.DARK_PURPLE).withStyle(ChatFormatting.BOLD)
                            .append(new TranslatableComponent("thitemstages.tooltip.stage.left_bracket").withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.DARK_PURPLE))
                            .append(new TranslatableComponent("thitemstages.tooltip.stage.stage", stage).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.BOLD))
                            .append(new TranslatableComponent("thitemstages.tooltip.stage.right_bracket").withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.DARK_PURPLE))
                    );
                    if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getCanPickup()){
                        event.getToolTip().add(new TranslatableComponent("thitemstages.tooltip.pickup").withStyle(ChatFormatting.WHITE));
                    }
                    if(RestrictionsData.getRestrictionData(s).getSettingsCodec().getCheckPlayerInventory()){
                        event.getToolTip().add(new TranslatableComponent("thitemstages.tooltip.playerinventory").withStyle(ChatFormatting.WHITE));
                    }
                    if(RestrictionsData.getRestrictionData(s).getSettingsCodec().getCheckPlayerEquipment()){
                        event.getToolTip().add(new TranslatableComponent("thitemstages.tooltip.playerequipment").withStyle(ChatFormatting.WHITE));
                    }
                    if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableItems()){
                        event.getToolTip().add(new TranslatableComponent("thitemstages.tooltip.usableitems").withStyle(ChatFormatting.WHITE));
                    }
                    if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableBlocks()){
                        event.getToolTip().add(new TranslatableComponent("thitemstages.tooltip.usableblocks").withStyle(ChatFormatting.WHITE));
                    }
                    if(RestrictionsData.getRestrictionData(s).getSettingsCodec().getHideInJEI()){
                        event.getToolTip().add(new TranslatableComponent("thitemstages.tooltip.hideinjei").withStyle(ChatFormatting.WHITE));
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public void onPlayerInteractWithBlock(PlayerInteractEvent.RightClickBlock event){
        if(event.getEntityLiving() instanceof Player){
            Player player = (Player) event.getEntityLiving();
            Set<ResourceLocation> blockTags = new HashSet<>();
            event.getWorld().getBlockState(event.getPos()).getTags().forEach(tag -> {
                blockTags.add(tag.location());
            });
            Registry.getRestrictions().forEach((s, x) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableBlocks() && !hasStage(player, stage)){
                    if(getBlocks(s).contains(event.getWorld().getBlockState(event.getPos()).getBlock().getRegistryName())){
                        event.setCanceled(true);
                        player.displayClientMessage(new TextComponent("You can't interact with this block!").withStyle(ChatFormatting.RED), true);
                    }

                    if(!getTags(s).isEmpty()){
                        for (ResourceLocation tagID : getTags(s)) {
                            if (blockTags.contains(tagID)) {
                                event.setCanceled(true);
                                player.displayClientMessage(new TextComponent("You can't interact with this block!").withStyle(ChatFormatting.RED), true);
                            }
                        }
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void onPlayerInteractWithBlock(PlayerInteractEvent.LeftClickBlock event){
        if(event.getEntityLiving() instanceof Player){
            Player player = (Player) event.getEntityLiving();
            Registry.getRestrictions().forEach((s, x) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableBlocks() && !hasStage(player, stage)){
                    if(checkAllItems(s, new ItemStack(event.getWorld().getBlockState(event.getPos()).getBlock().asItem()))) {
                        player.displayClientMessage(new TextComponent("You won't be able to use or destroy this block!").withStyle(ChatFormatting.RED), true);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void onPlayerDestroyBlock(BlockEvent.BreakEvent event){
        Registry.getRestrictions().forEach((s, x) -> {
            String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
            if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableBlocks() && !hasStage(event.getPlayer(), stage)){
                if(checkAllItems(s, new ItemStack(event.getWorld().getBlockState(event.getPos()).getBlock().asItem()))){
                    event.setCanceled(true);
                }
            }
        });
    }

    @SubscribeEvent
    public void onPlayerInteract(LivingAttackEvent event){
        if(event.getSource() != null && event.getSource().getEntity() instanceof Player && event.isCancelable() && event.getSource().getEntity() != null && !event.getSource().getEntity().level.isClientSide && !(event.getSource().getEntity() instanceof FakePlayer)){
            Player player = (Player) event.getSource().getEntity();
            Registry.getRestrictions().forEach((s, x) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableItems() && !hasStage(player, stage)){
                    if (getExceptions(s).contains(player.getMainHandItem().getItem().getRegistryName())) return;
                    if(checkAllItems(s, player.getMainHandItem())) {
                        event.setCanceled(true);
                        player.displayClientMessage(new TextComponent("You can't interact with this item!").withStyle(ChatFormatting.RED), true);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent event){
        if(event.isCancelable() && event.getPlayer() != null && !event.getPlayer().level.isClientSide && !(event.getPlayer() instanceof FakePlayer)){
            Player player = (Player) event.getEntityLiving();
            Registry.getRestrictions().forEach((s, x) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage();
                if(!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableItems() && !hasStage(player, stage)){
                    if(checkAllItems(s, event.getItemStack())) {
                        event.setCanceled(true);
                        player.displayClientMessage(new TextComponent("You can't interact with this item!").withStyle(ChatFormatting.RED), true);
                    }
                }
            });
        }
    }
}
