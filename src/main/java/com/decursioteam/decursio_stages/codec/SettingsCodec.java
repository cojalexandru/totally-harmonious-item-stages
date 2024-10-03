package com.decursioteam.decursio_stages.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class SettingsCodec {

    public static final SettingsCodec DEFAULT = new SettingsCodec("ALWAYS", "", 15,true, false, true, true, false, false, false,false, 0f);

    public static final Codec<SettingsCodec> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("advancedTooltips").orElse("ALWAYS").forGetter(SettingsCodec::getAdvancedTooltips),
            Codec.STRING.fieldOf("itemTitle").orElse("Unknown Item").forGetter(SettingsCodec::getItemTitle),
            Codec.INT.fieldOf("pickupDelay").orElse(60).forGetter(SettingsCodec::getPickupDelay),
            Codec.BOOL.fieldOf("hideInJEI").orElse(true).forGetter(SettingsCodec::getHideInJEI),
            Codec.BOOL.fieldOf("canPickup").orElse(false).forGetter(SettingsCodec::getCanPickup),
            Codec.BOOL.fieldOf("checkPlayerInventory").orElse(true).forGetter(SettingsCodec::getCheckPlayerInventory),
            Codec.BOOL.fieldOf("checkPlayerEquipment").orElse(true).forGetter(SettingsCodec::getCheckPlayerEquipment),
            Codec.BOOL.fieldOf("usableItems").orElse(false).forGetter(SettingsCodec::getUsableItems),
            Codec.BOOL.fieldOf("usableBlocks").orElse(false).forGetter(SettingsCodec::getUsableBlocks),
            Codec.BOOL.fieldOf("destroyableBlocks").orElse(false).forGetter(SettingsCodec::getDestroyableBlocks),
            Codec.BOOL.fieldOf("containerListWhitelist").orElse(false).forGetter(SettingsCodec::getHideInJEI),
            Codec.FLOAT.fieldOf("improvedMobsDifficulty").orElse(0.0F).forGetter(SettingsCodec::getImprovedMobsDifficulty)
    ).apply(instance, SettingsCodec::new));

    protected final String advancedTooltips;
    protected final String itemTitle;
    protected final int pickupDelay;
    protected final float improvedMobsDifficulty;
    protected final boolean hideInJEI;
    protected final boolean canPickup;
    protected final boolean checkPlayerInventory;
    protected final boolean checkPlayerEquipment;
    protected final boolean usableItems;
    protected final boolean usableBlocks;
    protected final boolean destroyableBlocks;
    protected final boolean containerListWhitelist;

    private SettingsCodec(String advancedTooltips, String itemTitle, int pickupDelay, boolean hideInJEI, boolean canPickup, boolean checkPlayerInventory, boolean checkPlayerEquipment, boolean usableItems, boolean usableBlocks, boolean destroyableBlocks, boolean containerListWhitelist, float improvedMobsDifficulty){
        this.advancedTooltips = advancedTooltips;
        this.itemTitle = itemTitle;
        this.pickupDelay = pickupDelay;
        this.hideInJEI = hideInJEI;
        this.canPickup = canPickup;
        this.checkPlayerInventory = checkPlayerInventory;
        this.checkPlayerEquipment = checkPlayerEquipment;
        this.usableItems = usableItems;
        this.usableBlocks = usableBlocks;
        this.destroyableBlocks = destroyableBlocks;
        this.containerListWhitelist = containerListWhitelist;
        this.improvedMobsDifficulty = improvedMobsDifficulty;
    }

    public String getAdvancedTooltips() {
        return advancedTooltips;
    }

    public float getImprovedMobsDifficulty() {
        return improvedMobsDifficulty;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public int getPickupDelay() {
        return pickupDelay;
    }

    public boolean getHideInJEI() {
        return hideInJEI;
    }

    public boolean getContainerListWhitelist() {
        return containerListWhitelist;
    }

    public boolean getCanPickup() {
        return canPickup;
    }

    public boolean getCheckPlayerInventory() {
        return checkPlayerInventory;
    }

    public boolean getCheckPlayerEquipment() {
        return checkPlayerEquipment;
    }

    public boolean getUsableItems() {
        return usableItems;
    }

    public boolean getUsableBlocks() {
        return usableBlocks;
    }

    public boolean getDestroyableBlocks() {
        return destroyableBlocks;
    }

    public SettingsCodec toImmutable() {
        return this;
    }

    public static class Mutable extends SettingsCodec {

        public Mutable(String advancedTooltips, String itemTitle, int pickupDelay, boolean hideInJEI, boolean canPickup, boolean checkPlayerInventory, boolean checkPlayerEquipment, boolean usableItems, boolean usableBlocks, boolean destroyableBlocks, boolean containerListWhitelist, float improvedMobsDifficulty) {
            super(advancedTooltips, itemTitle, pickupDelay, hideInJEI, canPickup, checkPlayerInventory, checkPlayerEquipment, usableItems, usableBlocks, destroyableBlocks, containerListWhitelist, improvedMobsDifficulty);
        }

        @Override
        public SettingsCodec toImmutable() {
            return new SettingsCodec(this.advancedTooltips, this.itemTitle, this.pickupDelay, this.hideInJEI, this.canPickup, this.checkPlayerInventory, this.checkPlayerEquipment, this.usableItems, this.usableBlocks, this.destroyableBlocks, this.containerListWhitelist, this.improvedMobsDifficulty);
        }
    }
}

