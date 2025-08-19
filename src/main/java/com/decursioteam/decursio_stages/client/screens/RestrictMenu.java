package com.decursioteam.decursio_stages.client.screens;

import com.decursioteam.decursio_stages.DecursioStages;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.common.extensions.IForgeMenuType;

import java.util.List;

public class RestrictMenu extends AbstractContainerMenu {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, DecursioStages.MOD_ID);
    public static final RegistryObject<MenuType<RestrictMenu>> RESTRICT_MENU = MENU_TYPES.register("restrict_menu",
            () -> IForgeMenuType.create((windowId, inv, data) -> new RestrictMenu(windowId, inv)));

    private final RestrictItemContainer container;

    private static final int SLOTS_PER_ROW = 9;
    private static final int VISIBLE_ROWS = 6;

    public RestrictMenu(int windowId, Inventory playerInventory) {
        super(RESTRICT_MENU.get(), windowId);
        this.container = new RestrictItemContainer();
        container.startOpen(playerInventory.player);
        addRestrictionSlots();
        addPlayerInventorySlots(playerInventory);
    }

    private void addRestrictionSlots() {
        int visibleSlots = Math.min(container.getContainerSize(), VISIBLE_ROWS * SLOTS_PER_ROW);

        for (int i = 0; i < visibleSlots; i++) {
            int row = i / SLOTS_PER_ROW;
            int col = i % SLOTS_PER_ROW;

            this.addSlot(new Slot(container, i, 8 + col * 18, 18 + row * 18) {
                @Override
                public void setChanged() {
                    super.setChanged();
                }
            });
        }
    }

    private void addPlayerInventorySlots(Inventory playerInventory) {
        int playerInvY = 18 + VISIBLE_ROWS * 18 + 14;

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, playerInvY + row * 18));
            }
        }

        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, playerInvY + 58));
        }
    }

    public int getRequiredHeight() {
        return 17 + (VISIBLE_ROWS * 18) + 14 + (3 * 18) + 18 + 7;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack resultStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            resultStack = slotStack.copy();

            int restrictionSlotsCount = Math.min(container.getContainerSize(), VISIBLE_ROWS * SLOTS_PER_ROW);

            if (index < restrictionSlotsCount) {
                if (!this.moveItemStackTo(slotStack, restrictionSlotsCount, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                int firstEmpty = container.getFirstEmptySlot();
                if (firstEmpty >= 0 && firstEmpty < restrictionSlotsCount) {
                    if (!this.moveItemStackTo(slotStack, firstEmpty, firstEmpty + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return resultStack;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public List<ItemStack> getRestrictedItems() {
        return container.getAllItems();
    }

    public ItemStack getRestrictedItem(int slot) {
        return container.getItem(slot);
    }

    public ItemStack getRestrictedItem() {
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.getItem(i);
            if (!stack.isEmpty()) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    public void setRestrictedItem(int slot, ItemStack stack) {
        container.setItem(slot, stack);
        this.broadcastChanges();
    }

    public void setRestrictedItem(ItemStack stack) {
        int firstEmpty = container.getFirstEmptySlot();
        if (firstEmpty >= 0) {
            container.setItem(firstEmpty, stack);
            this.broadcastChanges();
        }
    }

    public void clearItems() {
        container.clearContent();
        this.broadcastChanges();
    }
}