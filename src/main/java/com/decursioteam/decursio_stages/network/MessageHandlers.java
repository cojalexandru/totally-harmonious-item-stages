package com.decursioteam.decursio_stages.network;

import com.decursioteam.decursio_stages.DecursioStages;
import com.decursioteam.decursio_stages.Registry;
import com.decursioteam.decursio_stages.client.screens.RestrictMenu;
import com.decursioteam.decursio_stages.client.screens.RestrictScreen;
import com.decursioteam.decursio_stages.datagen.utils.FileUtils;
import com.decursioteam.decursio_stages.network.messages.OpenRestrictScreenMessage;
import com.decursioteam.decursio_stages.network.messages.SaveRestrictionMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class MessageHandlers {

    public static void handleOpenRestrictScreen(OpenRestrictScreenMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            context.enqueueWork(() -> openRestrictScreenClient(message));
        });

        context.setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void openRestrictScreenClient(OpenRestrictScreenMessage message) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            //DecursioStages.LOGGER.info("MessageHandlers: Opening restrict screen with windowId={}", message.getWindowId());

            try {
                RestrictMenu menu = new RestrictMenu(message.getWindowId(), minecraft.player.getInventory());

                minecraft.player.containerMenu = menu;
                RestrictScreen screen = new RestrictScreen(
                        menu,
                        minecraft.player.getInventory(),
                        Component.translatable("decursio_stages.gui.restrict.items_to_restrict"));

                minecraft.setScreen(screen);

                //DecursioStages.LOGGER.info("MessageHandlers: Successfully opened screen");
            } catch (Exception e) {
                //DecursioStages.LOGGER.error("MessageHandlers: Error opening restrict screen", e);
            }
        }
    }

    public static void handleSaveRestriction(SaveRestrictionMessage message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.hasPermissions(2)) {
                return; // Exit if player doesn't have permission
            }

            try {
                ItemStack itemStack = message.getItemStack();

                // If we have an item, add it to the restriction
                if (!itemStack.isEmpty()) {
                    // Then add the item to the restriction
                    boolean success = FileUtils.restrictItem(
                            message.getStage(),
                            message.getAdvancedTooltips(),
                            message.getItemTitle(),
                            message.getPickupDelay(),
                            message.isHideInJEI(),
                            message.isCanPickup(),
                            message.isContainerListWhitelist(),
                            message.isCheckPlayerInventory(),
                            message.isCheckPlayerEquipment(),
                            message.isUsableItems(),
                            message.isUsableBlocks(),
                            message.isDestroyableBlocks(),
                            itemStack
                    );

                    if (success) {
                        DecursioStages.LOGGER.info("Successfully restricted item {} for stage {}",
                                itemStack.getItem().toString(), message.getStage());

                        Registry.setupRestrictions();
                        Registry.registerRestrictionsList();

                        // Only send success message for non-empty items
                        player.sendSystemMessage(Component.translatable("decursio_stages.commands.restrictitem.success",
                                Component.literal(itemStack.getItem().toString()).withStyle(ChatFormatting.AQUA),
                                Component.literal(message.getStage()).withStyle(ChatFormatting.DARK_PURPLE)));
                    } else {
                        player.sendSystemMessage(Component.translatable("decursio_stages.commands.restrictitem.failure")
                                .withStyle(ChatFormatting.RED));
                    }
                }
            } catch (Exception e) {
                player.sendSystemMessage(Component.literal("Error creating restriction: " + e.getMessage())
                        .withStyle(ChatFormatting.RED));
                DecursioStages.LOGGER.error("MessageHandlers: Error processing restriction", e);
            }
        });
        context.setPacketHandled(true);
    }
}