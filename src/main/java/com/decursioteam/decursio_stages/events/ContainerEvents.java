package com.decursioteam.decursio_stages.events;

import com.decursioteam.decursio_stages.Registry;
import com.decursioteam.decursio_stages.config.CommonConfig;
import com.decursioteam.decursio_stages.datagen.RestrictionsData;
import com.decursioteam.decursio_stages.utils.ResourceUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static com.decursioteam.decursio_stages.utils.ResourceUtil.check;
import static com.decursioteam.decursio_stages.utils.ResourceUtil.getContainers;
import static com.decursioteam.decursio_stages.utils.StageUtil.hasStage;
import static com.decursioteam.decursio_stages.utils.Utils.dropItemStack;

public class ContainerEvents {
    private Set<ItemStack> prevInventory;

    @SubscribeEvent
    public void playerContainerOpenEvent(PlayerContainerEvent.Open event) {
        String containerName = event.getContainer().getClass().getName();
        if (Boolean.TRUE.equals(CommonConfig.debugMode.get()))
            event.getEntity().displayClientMessage(Component.literal(containerName), false);

        if (event.getEntity() instanceof ServerPlayer player) {

            Registry.getRestrictions().forEach((s, entityType) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage().toLowerCase(Locale.ROOT);
                if (!hasStage(player, stage)) {

                    if (!RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist() && getContainers(s).contains(containerName)) {
                        for (ItemStack item : event.getEntity().inventoryMenu.getItems()) {
                            if (check(s, item, ResourceUtil.CHECK_TYPES.ALL)) {
                                dropItemStack(player, item);
                            }
                        }
                    }
                    if (RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist() && !getContainers(s).contains(containerName)) {
                        for (ItemStack item : event.getEntity().inventoryMenu.getItems()) {
                            if (check(s, item, ResourceUtil.CHECK_TYPES.ALL)) {
                                dropItemStack(player, item);
                            }
                        }
                    }
                }
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public void clientInventoryTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() || event.player instanceof FakePlayer) return;
        Player player = event.player;

        // Check if player's inventory changed
        if (!new HashSet<>(player.getInventory().items).equals(prevInventory) || !player.inventoryMenu.getCraftSlots().isEmpty()) {
            Registry.getRestrictions().forEach((s, entityType) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage().toLowerCase(Locale.ROOT);

                // *Drop items from inventory crafting grid
                if (!hasStage(player, stage) && RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist() && !getContainers(s).contains("decursio_stages.inventoryMenu.CraftingGrid")) {
                    for (int i = 0; i <= player.inventoryMenu.getCraftSlots().getContainerSize(); i++) {
                        if (check(s, player.inventoryMenu.getCraftSlots().getItem(i), ResourceUtil.CHECK_TYPES.ALL)) {
                            dropItemStack(player, player.inventoryMenu.getCraftSlots().getItem(i));
                            player.inventoryMenu.slotsChanged(player.getInventory());
                        }
                    }
                }
                // *Drop items from inventory crafting grid
                if (!hasStage(player, stage) && !RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist() && getContainers(s).contains("decursio_stages.inventoryMenu.CraftingGrid")) {
                    for (int i = 0; i <= player.inventoryMenu.getCraftSlots().getContainerSize(); i++) {
                        if (check(s, player.inventoryMenu.getCraftSlots().getItem(i), ResourceUtil.CHECK_TYPES.ALL)) {
                            dropItemStack(player, player.inventoryMenu.getCraftSlots().getItem(i));
                            player.inventoryMenu.slotsChanged(player.getInventory());
                        }
                    }
                }

                // *Drop items from player's inventory
                if (RestrictionsData.getRestrictionData(s).getSettingsCodec().getCheckPlayerInventory() && !hasStage(player, stage)) {
                    for (ItemStack item : player.getInventory().items) {
                        if (check(s, item, ResourceUtil.CHECK_TYPES.ALL)) {
                            dropItemStack(player, item);
                        }
                    }
                }
            });
            prevInventory = new HashSet<>(player.getInventory().items);
        }
    }

    @SubscribeEvent
    public void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getEntity().getCommandSenderWorld().isClientSide()) return;
        if (event.getEntity() instanceof Player player) {
            Registry.getRestrictions().forEach((s, entityType) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage().toLowerCase(Locale.ROOT);
                if (RestrictionsData.getRestrictionData(s).getSettingsCodec().getCheckPlayerEquipment() && !hasStage(player, stage)) {
                    for (ItemStack item : player.getInventory().armor) {
                        if (check(s, item, ResourceUtil.CHECK_TYPES.ALL)) {
                            dropItemStack(player, item);
                        }
                    }
                }
            });
        }
    }
}
