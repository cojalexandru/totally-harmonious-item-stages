package com.decursioteam.decursio_stages.client.screens;

import com.decursioteam.decursio_stages.DecursioStages;
import com.decursioteam.decursio_stages.datagen.RestrictionsData;
import com.decursioteam.decursio_stages.datagen.utils.FileUtils;
import com.decursioteam.decursio_stages.network.messages.SaveRestrictionMessage;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class RestrictScreen extends AbstractContainerScreen<RestrictMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(DecursioStages.MOD_ID, "textures/gui/restrict_screen.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    public enum TooltipMode {
        ALWAYS("ALWAYS"),
        NONE("NONE"),
        ADVANCED("ADVANCED");

        private final String value;

        TooltipMode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static TooltipMode fromString(String text) {
            for (TooltipMode mode : TooltipMode.values()) {
                if (mode.value.equalsIgnoreCase(text)) {
                    return mode;
                }
            }
            return ALWAYS;
        }
    }

    private final int imageWidth = 176;
    private final int imageHeight;

    private EditBox stageField;
    private EditBox titleField;
    private EditBox pickupDelayField;

    private ToggleButton hideInJEIButton;
    private ToggleButton canPickupButton;
    private ToggleButton containerWhitelistButton;
    private ToggleButton checkInventoryButton;
    private ToggleButton checkEquipmentButton;
    private ToggleButton usableItemsButton;
    private ToggleButton usableBlocksButton;
    private ToggleButton destroyableBlocksButton;
    private TooltipModeButton advancedTooltipsButton;

    private List<String> restrictionsList = new ArrayList<>();
    private int scrollOffset = 0;
    private static final int MAX_VISIBLE_ENTRIES = 7;
    private Button scrollUpButton;
    private Button scrollDownButton;
    private List<RestrictionListEntry> listEntries = new ArrayList<>();

    private Button saveButton;
    private Button clearButton;
    private Button deleteButton;

    private String selectedRestrictionName = null;
    private boolean isUpdatingFromSelection = false;

    public RestrictScreen(RestrictMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = menu.getRequiredHeight();
        this.inventoryLabelY = 18 + (6 * 18) + 5;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
        this.inventoryLabelX = 8;
    }

    @Override
    protected void init() {
        this.leftPos = 20;
        this.topPos = 20;

        selectedRestrictionName = null;
        int textY = topPos + 10;
        int rightColumnX = leftPos + imageWidth + 15;

        this.stageField = new EditBox(this.font, rightColumnX, textY + 20, 110, 16, Component.translatable("decursio_stages.gui.restrict.stage"));
        this.stageField.setMaxLength(32);
        this.stageField.setValue("");
        this.stageField.setTooltip(Tooltip.create(Component.translatable("decursio_stages.gui.restrict.stage.tooltip")));

        this.titleField = new EditBox(this.font, rightColumnX, textY + 50, 110, 16, Component.translatable("decursio_stages.gui.restrict.item_title"));
        this.titleField.setMaxLength(32);
        this.titleField.setValue("Locked Item");
        this.titleField.setTooltip(Tooltip.create(Component.translatable("decursio_stages.gui.restrict.item_title.tooltip")));

        this.pickupDelayField = new EditBox(this.font, rightColumnX, textY + 80, 50, 16, Component.translatable("decursio_stages.gui.restrict.pickup_delay"));
        this.pickupDelayField.setMaxLength(3);
        this.pickupDelayField.setValue("40");
        this.pickupDelayField.setFilter(s -> s.matches("\\d*"));
        this.pickupDelayField.setTooltip(Tooltip.create(Component.translatable("decursio_stages.gui.restrict.pickup_delay.tooltip")));

        this.addWidget(this.stageField);
        this.addWidget(this.titleField);
        this.addWidget(this.pickupDelayField);

        int toggleX = rightColumnX;
        int toggleY = textY + 120;
        int toggleSpacing = 20;
        int toggleWidth = 130;

        this.hideInJEIButton = new ToggleButton(toggleX, toggleY, toggleWidth, 16,
                Component.translatable("decursio_stages.gui.restrict.hide_in_jei"), true);
        this.hideInJEIButton.setTooltip(Tooltip.create(Component.translatable("decursio_stages.gui.restrict.hide_in_jei.tooltip")));

        this.canPickupButton = new ToggleButton(toggleX, toggleY + toggleSpacing, toggleWidth, 16,
                Component.translatable("decursio_stages.gui.restrict.can_pickup"), false);
        this.canPickupButton.setTooltip(Tooltip.create(Component.translatable("decursio_stages.gui.restrict.can_pickup.tooltip")));

        this.containerWhitelistButton = new ToggleButton(toggleX, toggleY + toggleSpacing * 2, toggleWidth, 16,
                Component.translatable("decursio_stages.gui.restrict.container_whitelist"), true);
        this.containerWhitelistButton.setTooltip(Tooltip.create(Component.translatable("decursio_stages.gui.restrict.container_whitelist.tooltip")));

        this.checkInventoryButton = new ToggleButton(toggleX, toggleY + toggleSpacing * 3, toggleWidth, 16,
                Component.translatable("decursio_stages.gui.restrict.check_inventory"), true);
        this.checkInventoryButton.setTooltip(Tooltip.create(Component.translatable("decursio_stages.gui.restrict.check_inventory.tooltip")));

        this.checkEquipmentButton = new ToggleButton(toggleX, toggleY + toggleSpacing * 4, toggleWidth, 16,
                Component.translatable("decursio_stages.gui.restrict.check_equipment"), true);
        this.checkEquipmentButton.setTooltip(Tooltip.create(Component.translatable("decursio_stages.gui.restrict.check_equipment.tooltip")));

        this.usableItemsButton = new ToggleButton(toggleX, toggleY + toggleSpacing * 5, toggleWidth, 16,
                Component.translatable("decursio_stages.gui.restrict.usable_items"), false);
        this.usableItemsButton.setTooltip(Tooltip.create(Component.translatable("decursio_stages.gui.restrict.usable_items.tooltip")));

        this.usableBlocksButton = new ToggleButton(toggleX, toggleY + toggleSpacing * 6, toggleWidth, 16,
                Component.translatable("decursio_stages.gui.restrict.usable_blocks"), false);
        this.usableBlocksButton.setTooltip(Tooltip.create(Component.translatable("decursio_stages.gui.restrict.usable_blocks.tooltip")));

        this.destroyableBlocksButton = new ToggleButton(toggleX, toggleY + toggleSpacing * 7, toggleWidth, 16,
                Component.translatable("decursio_stages.gui.restrict.destroyable_blocks"), false);
        this.destroyableBlocksButton.setTooltip(Tooltip.create(Component.translatable("decursio_stages.gui.restrict.destroyable_blocks.tooltip")));

        this.advancedTooltipsButton = new TooltipModeButton(toggleX, toggleY + toggleSpacing * 8, toggleWidth, 16,
                Component.translatable("decursio_stages.gui.restrict.advanced_tooltips"), TooltipMode.ALWAYS);
        this.advancedTooltipsButton.setTooltip(Tooltip.create(Component.translatable("decursio_stages.gui.restrict.advanced_tooltips.tooltip")));

        this.addRenderableWidget(this.hideInJEIButton);
        this.addRenderableWidget(this.canPickupButton);
        this.addRenderableWidget(this.containerWhitelistButton);
        this.addRenderableWidget(this.checkInventoryButton);
        this.addRenderableWidget(this.checkEquipmentButton);
        this.addRenderableWidget(this.usableItemsButton);
        this.addRenderableWidget(this.usableBlocksButton);
        this.addRenderableWidget(this.destroyableBlocksButton);
        this.addRenderableWidget(this.advancedTooltipsButton);

        this.updateRestrictionsList();
        this.updateListEntries();
    }

    private void updateRestrictionsList() {
        restrictionsList.clear();
        RestrictionsData.getRegistry().getRawRestrictions().forEach((name, jsonObject) -> {
            restrictionsList.add(name);
        });
    }

    private void updateListEntries() {
        this.listEntries.forEach(this::removeWidget);
        this.listEntries.clear();

        int rightColumnX = leftPos + imageWidth + 5;
        int farRightColumnX = rightColumnX + 140;

        if (scrollUpButton != null) {
            this.removeWidget(scrollUpButton);
        }
        if (scrollDownButton != null) {
            this.removeWidget(scrollDownButton);
        }

        int listStartY = topPos + 40;
        int listHeight = MAX_VISIBLE_ENTRIES * 18;
        int listEndY = listStartY + listHeight;

        int entryListWidth = 110;
        int scrollButtonCenterX = farRightColumnX + (entryListWidth / 2) - 10;

        this.scrollUpButton = Button.builder(Component.literal("▲"), this::onScrollUpClicked)
                .pos(scrollButtonCenterX, listStartY - 20)
                .size(20, 20)
                .build();

        this.scrollDownButton = Button.builder(Component.literal("▼"), this::onScrollDownClicked)
                .pos(scrollButtonCenterX, listEndY + 5)
                .size(20, 20)
                .build();

        this.addRenderableWidget(this.scrollUpButton);
        this.addRenderableWidget(this.scrollDownButton);

        int listX = farRightColumnX;
        int listY = listStartY + 5;
        int listWidth = 110;
        int listItemHeight = 16;
        int listSpacing = 18;

        int endIndex = Math.min(scrollOffset + MAX_VISIBLE_ENTRIES, restrictionsList.size());
        for (int i = scrollOffset; i < endIndex; i++) {
            String restrictionName = restrictionsList.get(i);
            RestrictionListEntry entry = new RestrictionListEntry(
                    listX,
                    listY + (i - scrollOffset) * listSpacing,
                    listWidth,
                    listItemHeight,
                    restrictionName,
                    this::onRestrictionSelected);
            this.listEntries.add(entry);
            this.addRenderableWidget(entry);
        }

        this.scrollUpButton.active = scrollOffset > 0;
        this.scrollDownButton.active = scrollOffset + MAX_VISIBLE_ENTRIES < restrictionsList.size();

        int buttonY = listEndY + 30;

        this.clearButton = Button.builder(Component.translatable("decursio_stages.gui.restrict.clear"), this::onClearClicked)
                .pos(farRightColumnX, buttonY)
                .size(110, 20)
                .build();

        this.deleteButton = Button.builder(
                        Component.translatable("decursio_stages.gui.restrict.delete").withStyle(ChatFormatting.RED),
                        this::onDeleteClicked)
                .pos(farRightColumnX, buttonY + 25)
                .size(53, 20)
                .build();

        this.saveButton = Button.builder(Component.translatable("decursio_stages.gui.restrict.save"), this::onSaveClicked)
                .pos(farRightColumnX + 57, buttonY + 25)
                .size(53, 20)
                .build();

        this.addRenderableWidget(this.clearButton);
        this.addRenderableWidget(this.deleteButton);
        this.addRenderableWidget(this.saveButton);
    }

    private void onScrollUpClicked(Button button) {
        if (scrollOffset > 0) {
            scrollOffset--;
            updateListEntries();
        }
    }

    private void onScrollDownClicked(Button button) {
        if (scrollOffset + MAX_VISIBLE_ENTRIES < restrictionsList.size()) {
            scrollOffset++;
            updateListEntries();
        }
    }

    private void onRestrictionSelected(String restrictionName) {
        JsonObject restriction = RestrictionsData.getRegistry().getRawORestrictionsData(restrictionName);
        if (restriction != null) {
            try {
                isUpdatingFromSelection = true;
                selectedRestrictionName = restrictionName;

                String stage = restriction.getAsJsonObject("Restriction Data").get("stage").getAsString();
                stageField.setValue(stage);

                JsonObject settings = restriction.getAsJsonObject("Settings");

                titleField.setValue(settings.get("itemsTitle").getAsString());
                pickupDelayField.setValue(String.valueOf(settings.get("itemsPickupDelay").getAsInt()));

                hideInJEIButton.setValue(settings.get("hideInJEI_REI").getAsBoolean());
                canPickupButton.setValue(settings.get("canPickupItems").getAsBoolean());
                containerWhitelistButton.setValue(settings.get("containerListWhitelist").getAsBoolean());
                checkInventoryButton.setValue(settings.get("dropItemsFromInventory").getAsBoolean());
                checkEquipmentButton.setValue(settings.get("dropArmorFromInventory").getAsBoolean());
                usableItemsButton.setValue(settings.get("canUseItems").getAsBoolean());
                usableBlocksButton.setValue(settings.get("canRightClickBlocks").getAsBoolean());
                destroyableBlocksButton.setValue(settings.get("canBreakBlocks").getAsBoolean());

                String tooltipSetting = settings.has("advancedTooltips") ?
                        settings.get("advancedTooltips").getAsString() : "ALWAYS";
                advancedTooltipsButton.setValue(TooltipMode.fromString(tooltipSetting));

                menu.clearItems();

                Minecraft.getInstance().player.displayClientMessage(
                        Component.translatable("decursio_stages.gui.restrict.loaded", restrictionName)
                                .withStyle(ChatFormatting.GREEN),
                        false);
            } finally {
                isUpdatingFromSelection = false;
            }
        }
    }

    private void onFieldChanged(String newValue) {
        if (selectedRestrictionName != null && !isUpdatingFromSelection) {
            updateSelectedRestriction();
        }
    }

    private void onToggleChanged(String propertyName, Object newValue) {
        if (selectedRestrictionName != null && !isUpdatingFromSelection) {
            updateSelectedRestriction();
        }
    }

    private void onDeleteClicked(Button button) {
        if (selectedRestrictionName == null) {
            Minecraft.getInstance().player.displayClientMessage(
                    Component.translatable("decursio_stages.gui.restrict.delete.no_selection")
                            .withStyle(ChatFormatting.RED),
                    false);
            return;
        }

        try {
            Path filePath = Paths.get(FileUtils.createCustomPath("restrictions") + "/" + selectedRestrictionName + ".json");
            Files.deleteIfExists(filePath);

            RestrictionsData.getRegistry().clearCustomRestrictionData();
            RestrictionsData.getRegistry().regenerateCustomRestrictionData();

            onClearClicked(null);
            selectedRestrictionName = null;

            updateRestrictionsList();
            updateListEntries();

            Minecraft.getInstance().player.displayClientMessage(
                    Component.translatable("decursio_stages.gui.restrict.delete.success", selectedRestrictionName)
                            .withStyle(ChatFormatting.GREEN),
                    false);

        } catch (Exception e) {
            DecursioStages.LOGGER.error("Error deleting restriction: {}", selectedRestrictionName, e);
            Minecraft.getInstance().player.displayClientMessage(
                    Component.translatable("decursio_stages.gui.restrict.delete.error")
                            .withStyle(ChatFormatting.RED),
                    false);
        }
    }

    private void updateSelectedRestriction() {
        if (selectedRestrictionName == null) return;

        try {
            JsonObject restriction = RestrictionsData.getRegistry().getRawORestrictionsData(selectedRestrictionName);
            if (restriction == null) return;

            String stage = stageField.getValue().trim();
            if (!stage.isEmpty()) {
                restriction.getAsJsonObject("Restriction Data").addProperty("stage", stage);
            }

            JsonObject settings = restriction.getAsJsonObject("Settings");
            settings.addProperty("itemsTitle", titleField.getValue());

            try {
                int pickupDelay = Integer.parseInt(pickupDelayField.getValue());
                settings.addProperty("itemsPickupDelay", pickupDelay);
            } catch (NumberFormatException e) {
            }

            settings.addProperty("hideInJEI_REI", hideInJEIButton.getValue());
            settings.addProperty("canPickupItems", canPickupButton.getValue());
            settings.addProperty("containerListWhitelist", containerWhitelistButton.getValue());
            settings.addProperty("dropItemsFromInventory", checkInventoryButton.getValue());
            settings.addProperty("dropArmorFromInventory", checkEquipmentButton.getValue());
            settings.addProperty("canUseItems", usableItemsButton.getValue());
            settings.addProperty("canRightClickBlocks", usableBlocksButton.getValue());
            settings.addProperty("canBreakBlocks", destroyableBlocksButton.getValue());
            settings.addProperty("advancedTooltips", advancedTooltipsButton.getValue().getValue());

            FileUtils.saveRestrictionFile(selectedRestrictionName, restriction);

            Minecraft.getInstance().player.displayClientMessage(
                    Component.translatable("decursio_stages.gui.restrict.update_success", selectedRestrictionName)
                            .withStyle(ChatFormatting.GREEN),
                    false);

        } catch (Exception e) {
            DecursioStages.LOGGER.error("Error updating restriction: {}", selectedRestrictionName, e);
            Minecraft.getInstance().player.displayClientMessage(
                    Component.translatable("decursio_stages.gui.restrict.update_error")
                            .withStyle(ChatFormatting.RED),
                    false);
        }
    }

    private void onSaveClicked(Button button) {
        String stage = stageField.getValue().trim();
        String itemTitle = titleField.getValue().trim();
        int pickupDelay;

        try {
            pickupDelay = Integer.parseInt(pickupDelayField.getValue());
        } catch (NumberFormatException e) {
            pickupDelay = 40;
        }

        boolean hideInJEI = hideInJEIButton.getValue();
        boolean canPickup = canPickupButton.getValue();
        boolean containerWhitelist = containerWhitelistButton.getValue();
        boolean checkInventory = checkInventoryButton.getValue();
        boolean checkEquipment = checkEquipmentButton.getValue();
        boolean usableItems = usableItemsButton.getValue();
        boolean usableBlocks = usableBlocksButton.getValue();
        boolean destroyableBlocks = destroyableBlocksButton.getValue();
        String advancedTooltips = advancedTooltipsButton.getValue().getValue();

        List<ItemStack> itemStacks = menu.getRestrictedItems();

        if (stage.isEmpty()) {
            Minecraft.getInstance().player.displayClientMessage(
                    Component.translatable("decursio_stages.gui.restrict.error.no_stage").withStyle(ChatFormatting.RED),
                    false);
            return;
        }

        if (itemTitle.isEmpty()) {
            Minecraft.getInstance().player.displayClientMessage(
                    Component.translatable("decursio_stages.gui.restrict.error.no_title").withStyle(ChatFormatting.RED),
                    false);
            return;
        }

        if (itemStacks.isEmpty()) {
            Minecraft.getInstance().player.displayClientMessage(
                    Component.translatable("decursio_stages.gui.restrict.error.no_item").withStyle(ChatFormatting.RED),
                    false);
            return;
        }

        boolean isUpdate = selectedRestrictionName != null;

        FileUtils.addRestriction(stage, advancedTooltips, itemTitle, pickupDelay, hideInJEI,
                canPickup, containerWhitelist, checkInventory, checkEquipment,
                usableItems, usableBlocks, destroyableBlocks);

        for (ItemStack item : itemStacks) {
            if (!item.isEmpty()) {
                DecursioStages.NETWORK.sendToServer(new SaveRestrictionMessage(
                        stage, advancedTooltips, itemTitle, pickupDelay, hideInJEI, canPickup,
                        containerWhitelist, checkInventory, checkEquipment, usableItems,
                        usableBlocks, destroyableBlocks, item
                ));
            }
        }

        this.updateRestrictionsList();

        Minecraft.getInstance().player.displayClientMessage(
                Component.translatable(isUpdate ?
                                        "decursio_stages.gui.restrict.update_success" :
                                        "decursio_stages.gui.restrict.success",
                                itemStacks.size())
                        .withStyle(ChatFormatting.GREEN),
                false);

        this.onClose();
    }

    private void onClearClicked(Button button) {
        stageField.setValue("");
        titleField.setValue("Locked Item");
        pickupDelayField.setValue("40");

        hideInJEIButton.setValue(true);
        canPickupButton.setValue(false);
        containerWhitelistButton.setValue(true);
        checkInventoryButton.setValue(true);
        checkEquipmentButton.setValue(true);
        usableItemsButton.setValue(false);
        usableBlocksButton.setValue(false);
        destroyableBlocksButton.setValue(false);
        advancedTooltipsButton.setValue(TooltipMode.ALWAYS);

        menu.clearItems();
        selectedRestrictionName = null;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);

        int rightColumnX = leftPos + imageWidth + 5;
        int farRightColumnX = rightColumnX + 140;
        int panelWidth = 260;

        int listHeight = MAX_VISIBLE_ENTRIES * 18;
        int buttonAreaHeight = 30 + 20 + 5 + 20;
        int requiredHeight = Math.max(imageHeight + 100, listHeight + buttonAreaHeight + 90);

        graphics.fill(rightColumnX, topPos, rightColumnX + panelWidth, topPos + requiredHeight, 0x80000000);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        this.renderBg(graphics, partialTick, mouseX, mouseY);

        int rightColumnX = leftPos + imageWidth + 15;
        int farRightColumnX = rightColumnX + 140;
        int textY = topPos;

        graphics.drawString(this.font, Component.translatable("decursio_stages.gui.restrict.stage"), rightColumnX, textY + 20, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.translatable("decursio_stages.gui.restrict.item_title"), rightColumnX, textY + 50, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.translatable("decursio_stages.gui.restrict.pickup_delay"), rightColumnX, textY + 80, 0xFFFFFF, false);
        graphics.drawString(this.font, Component.translatable("decursio_stages.gui.restrict.settings"), rightColumnX, textY + 120, 0xFFFFFF, false);

        graphics.drawString(this.font, Component.translatable("decursio_stages.gui.restrict.existing"), farRightColumnX, topPos + 10, 0xFFFFFF, false);

        super.render(graphics, mouseX, mouseY, partialTick);

        this.stageField.render(graphics, mouseX, mouseY, partialTick);
        this.titleField.render(graphics, mouseX, mouseY, partialTick);
        this.pickupDelayField.render(graphics, mouseX, mouseY, partialTick);
    }

    private static class ToggleButton extends Button {
        private boolean value;
        private final String baseText;
        private Consumer<Boolean> changeListener;

        public ToggleButton(int x, int y, int width, int height, Component text, boolean defaultValue) {
            super(x, y, width, height, text, button -> {}, DEFAULT_NARRATION);
            this.value = defaultValue;
            this.baseText = text.getString();
            updateMessage();
        }

        private void updateMessage() {
            String status = value ? ChatFormatting.GREEN + "ON" : ChatFormatting.RED + "OFF";
            this.setMessage(Component.literal(baseText + ": " + status));
        }

        @Override
        public void onPress() {
            super.onPress();
            value = !value;
            updateMessage();
            if (changeListener != null) {
                changeListener.accept(value);
            }
        }

        public boolean getValue() {
            return value;
        }

        public void setValue(boolean value) {
            boolean changed = this.value != value;
            this.value = value;
            updateMessage();
            if (changed && changeListener != null) {
                changeListener.accept(value);
            }
        }

        public void setChangeListener(Consumer<Boolean> listener) {
            this.changeListener = listener;
        }
    }

    private static class TooltipModeButton extends Button {
        private TooltipMode value;
        private final String baseText;
        private Consumer<TooltipMode> changeListener;

        public TooltipModeButton(int x, int y, int width, int height, Component text, TooltipMode defaultValue) {
            super(x, y, width, height, text, button -> {}, DEFAULT_NARRATION);
            this.value = defaultValue;
            this.baseText = text.getString();
            updateMessage();
        }

        private void updateMessage() {
            ChatFormatting color;
            switch (value) {
                case ALWAYS:
                    color = ChatFormatting.GREEN;
                    break;
                case NONE:
                    color = ChatFormatting.RED;
                    break;
                case ADVANCED:
                    color = ChatFormatting.GOLD;
                    break;
                default:
                    color = ChatFormatting.WHITE;
            }

            this.setMessage(Component.literal(baseText + ": " + color + value.getValue()));
        }

        @Override
        public void onPress() {
            super.onPress();
            switch (value) {
                case ALWAYS:
                    value = TooltipMode.NONE;
                    break;
                case NONE:
                    value = TooltipMode.ADVANCED;
                    break;
                case ADVANCED:
                    value = TooltipMode.ALWAYS;
                    break;
            }

            updateMessage();
            if (changeListener != null) {
                changeListener.accept(value);
            }
        }

        public TooltipMode getValue() {
            return value;
        }

        public void setValue(TooltipMode value) {
            boolean changed = this.value != value;
            this.value = value;
            updateMessage();
            if (changed && changeListener != null) {
                changeListener.accept(value);
            }
        }

        public void setChangeListener(Consumer<TooltipMode> listener) {
            this.changeListener = listener;
        }
    }

    private static class RestrictionListEntry extends Button {
        private final String restrictionName;
        private final Consumer<String> onSelected;

        public RestrictionListEntry(int x, int y, int width, int height, String restrictionName, Consumer<String> onSelected) {
            super(x, y, width, height, Component.literal(restrictionName), button -> {}, DEFAULT_NARRATION);
            this.restrictionName = restrictionName;
            this.onSelected = onSelected;
        }

        @Override
        public void onPress() {
            super.onPress();
            onSelected.accept(restrictionName);
        }
    }
}