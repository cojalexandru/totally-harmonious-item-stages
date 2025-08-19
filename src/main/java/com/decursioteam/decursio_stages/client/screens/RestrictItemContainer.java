package com.decursioteam.decursio_stages.client.screens;

import com.decursioteam.decursio_stages.DecursioStages;
import com.google.common.collect.Lists;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.ContainerListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class RestrictItemContainer implements Container {
    private static final int CONTAINER_SIZE = 54;

    private final NonNullList<ItemStack> items;
    @Nullable
    private List<ContainerListener> listeners;

    public RestrictItemContainer() {
        this.items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);
    }

    public void addListener(ContainerListener listener) {
        if (this.listeners == null) {
            this.listeners = Lists.newArrayList();
        }
        this.listeners.add(listener);
    }

    public void removeListener(ContainerListener listener) {
        if (this.listeners != null) {
            this.listeners.remove(listener);
        }
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public int getFirstEmptySlot() {
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            if (items.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public List<ItemStack> getAllItems() {
        List<ItemStack> result = new ArrayList<>();
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                result.add(stack.copy());
            }
        }
        return result;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot >= 0 && slot < CONTAINER_SIZE) {
            return items.get(slot);
        }
        DecursioStages.LOGGER.warn("RestrictItemContainer: Attempted to get item from invalid slot {}", slot);
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot >= 0 && slot < CONTAINER_SIZE) {
            ItemStack result = ContainerHelper.removeItem(this.items, slot, amount);
            if (!result.isEmpty()) {
                this.setChanged();
            }
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot >= 0 && slot < CONTAINER_SIZE) {
            ItemStack result = items.get(slot);
            items.set(slot, ItemStack.EMPTY);
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < CONTAINER_SIZE) {
            this.items.set(slot, stack);
            if (!stack.isEmpty() && stack.getCount() > this.getMaxStackSize()) {
                stack.setCount(this.getMaxStackSize());
            }

            this.setChanged();
        } else {
            DecursioStages.LOGGER.warn("RestrictItemContainer: Attempted to set item in invalid slot {}", slot);
        }
    }

    @Override
    public void setChanged() {
        if (this.listeners != null) {
            for (ContainerListener listener : this.listeners) {
                listener.containerChanged(this);
            }
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            items.set(i, ItemStack.EMPTY);
        }
        this.setChanged();
    }
}