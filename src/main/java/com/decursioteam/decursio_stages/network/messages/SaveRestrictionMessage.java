package com.decursioteam.decursio_stages.network.messages;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

/**
 * Message to send restriction data from client to server for processing
 */
public class SaveRestrictionMessage {
    private final String stage;
    private final String advancedTooltips;
    private final String itemTitle;
    private final int pickupDelay;
    private final boolean hideInJEI;
    private final boolean canPickup;
    private final boolean containerListWhitelist;
    private final boolean checkPlayerInventory;
    private final boolean checkPlayerEquipment;
    private final boolean usableItems;
    private final boolean usableBlocks;
    private final boolean destroyableBlocks;
    private final ItemStack itemStack;

    public SaveRestrictionMessage(
            String stage,
            String advancedTooltips,
            String itemTitle,
            int pickupDelay,
            boolean hideInJEI,
            boolean canPickup,
            boolean containerListWhitelist,
            boolean checkPlayerInventory,
            boolean checkPlayerEquipment,
            boolean usableItems,
            boolean usableBlocks,
            boolean destroyableBlocks,
            ItemStack itemStack) {
        this.stage = stage;
        this.advancedTooltips = advancedTooltips;
        this.itemTitle = itemTitle;
        this.pickupDelay = pickupDelay;
        this.hideInJEI = hideInJEI;
        this.canPickup = canPickup;
        this.containerListWhitelist = containerListWhitelist;
        this.checkPlayerInventory = checkPlayerInventory;
        this.checkPlayerEquipment = checkPlayerEquipment;
        this.usableItems = usableItems;
        this.usableBlocks = usableBlocks;
        this.destroyableBlocks = destroyableBlocks;
        this.itemStack = itemStack;
    }

    public SaveRestrictionMessage(FriendlyByteBuf buffer) {
        this.stage = buffer.readUtf();
        this.advancedTooltips = buffer.readUtf();
        this.itemTitle = buffer.readUtf();
        this.pickupDelay = buffer.readInt();
        this.hideInJEI = buffer.readBoolean();
        this.canPickup = buffer.readBoolean();
        this.containerListWhitelist = buffer.readBoolean();
        this.checkPlayerInventory = buffer.readBoolean();
        this.checkPlayerEquipment = buffer.readBoolean();
        this.usableItems = buffer.readBoolean();
        this.usableBlocks = buffer.readBoolean();
        this.destroyableBlocks = buffer.readBoolean();
        this.itemStack = buffer.readItem();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeUtf(stage);
        buffer.writeUtf(advancedTooltips);
        buffer.writeUtf(itemTitle);
        buffer.writeInt(pickupDelay);
        buffer.writeBoolean(hideInJEI);
        buffer.writeBoolean(canPickup);
        buffer.writeBoolean(containerListWhitelist);
        buffer.writeBoolean(checkPlayerInventory);
        buffer.writeBoolean(checkPlayerEquipment);
        buffer.writeBoolean(usableItems);
        buffer.writeBoolean(usableBlocks);
        buffer.writeBoolean(destroyableBlocks);
        buffer.writeItem(itemStack);
    }

    public String getStage() {
        return stage;
    }

    public String getAdvancedTooltips() {
        return advancedTooltips;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public int getPickupDelay() {
        return pickupDelay;
    }

    public boolean isHideInJEI() {
        return hideInJEI;
    }

    public boolean isCanPickup() {
        return canPickup;
    }

    public boolean isContainerListWhitelist() {
        return containerListWhitelist;
    }

    public boolean isCheckPlayerInventory() {
        return checkPlayerInventory;
    }

    public boolean isCheckPlayerEquipment() {
        return checkPlayerEquipment;
    }

    public boolean isUsableItems() {
        return usableItems;
    }

    public boolean isUsableBlocks() {
        return usableBlocks;
    }

    public boolean isDestroyableBlocks() {
        return destroyableBlocks;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }
}