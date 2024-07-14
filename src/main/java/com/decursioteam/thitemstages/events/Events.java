package com.decursioteam.thitemstages.events;

import com.decursioteam.thitemstages.Registry;
import com.decursioteam.thitemstages.THItemStages;
import com.decursioteam.thitemstages.config.CommonConfig;
import com.decursioteam.thitemstages.datagen.RestrictionsData;
import com.decursioteam.thitemstages.datagen.utils.IStagesData;
import com.decursioteam.thitemstages.mobstaging.EffectsCodec;
import com.decursioteam.thitemstages.mobstaging.MobRestriction;
import com.decursioteam.thitemstages.utils.StageUtil;
import com.decursioteam.thitemstages.utils.Utils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureFeatureIndexSavedData;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.CustomizeGuiOverlayEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.MobSpawnEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.decursioteam.thitemstages.utils.ResourceUtil.*;
import static com.decursioteam.thitemstages.utils.StageUtil.hasStage;
import static com.decursioteam.thitemstages.utils.Utils.ITEM_INTERACT_ERROR;
import static com.decursioteam.thitemstages.utils.Utils.dropItemStack;

public class Events {

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
                            if (check(s, item, CHECK_TYPES.ALL)) {
                                dropItemStack(player, item);
                            }
                        }
                    }
                    if (RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist() && !getContainers(s).contains(containerName)) {
                        for (ItemStack item : event.getEntity().inventoryMenu.getItems()) {
                            if (check(s, item, CHECK_TYPES.ALL)) {
                                dropItemStack(player, item);
                            }
                        }
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void entityTravelToDimension(EntityTravelToDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Registry.getRestrictions().forEach((s, entityType) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage().toLowerCase(Locale.ROOT);
                if (!RestrictionsData.getRestrictionData(s).getData().getDimensionList().isEmpty() && !hasStage(player, stage)) {
                    getDimensions(s).forEach(dimensionRestriction -> {
                        if (dimensionRestriction.getDimension().equals(event.getDimension().location())) {
                            player.displayClientMessage(Component.literal(dimensionRestriction.getMessage()).withStyle(ChatFormatting.RED), true);
                            event.setCanceled(true);
                        }
                    });
                }
            });
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
                        if (check(s, item, CHECK_TYPES.ALL)) {
                            dropItemStack(player, item);
                        }
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void onItemPickupEvent(EntityItemPickupEvent event) {
        if (event.getEntity() != null) {
            ServerPlayer player = (ServerPlayer) event.getEntity();
            ItemStack item = event.getItem().getItem();
            Registry.getRestrictions().forEach((s, entityType) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage().toLowerCase(Locale.ROOT).toLowerCase(Locale.ROOT);
                if (!hasStage(player, stage) && player.containerMenu != player.inventoryMenu) {
                    if (!RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist()) {
                        if (getContainers(s).contains(player.containerMenu.getClass().getName())) {
                            if (check(s, item, CHECK_TYPES.ALL)) {
                                dropItemStack(event, s, player, false);
                            }
                        }
                    }
                    if (RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist()) {
                        if (!getContainers(s).contains(player.containerMenu.getClass().getName())) {
                            if (check(s, item, CHECK_TYPES.ALL)) {
                                dropItemStack(event, s, player, false);
                            }
                        }
                    }
                }
                if (!RestrictionsData.getRestrictionData(s).getSettingsCodec().getCanPickup() && !hasStage(player, stage)) {
                    if (check(s, item, CHECK_TYPES.ALL)) {
                        dropItemStack(event, s, player, true);
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
                if (!hasStage(player, stage) && RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist() && !getContainers(s).contains("thitemstages.inventoryMenu.CraftingGrid")) {
                    for (int i = 0; i <= player.inventoryMenu.getCraftSlots().getContainerSize(); i++) {
                        if (check(s, player.inventoryMenu.getCraftSlots().getItem(i), CHECK_TYPES.ALL)) {
                            dropItemStack(player, player.inventoryMenu.getCraftSlots().getItem(i));
                            player.inventoryMenu.slotsChanged(player.getInventory());
                        }
                    }
                }
                // *Drop items from inventory crafting grid
                if (!hasStage(player, stage) && !RestrictionsData.getRestrictionData(s).getSettingsCodec().getContainerListWhitelist() && getContainers(s).contains("thitemstages.inventoryMenu.CraftingGrid")) {
                    for (int i = 0; i <= player.inventoryMenu.getCraftSlots().getContainerSize(); i++) {
                        if (check(s, player.inventoryMenu.getCraftSlots().getItem(i), CHECK_TYPES.ALL)) {
                            dropItemStack(player, player.inventoryMenu.getCraftSlots().getItem(i));
                            player.inventoryMenu.slotsChanged(player.getInventory());
                        }
                    }
                }

                // *Drop items from player's inventory
                if (RestrictionsData.getRestrictionData(s).getSettingsCodec().getCheckPlayerInventory() && !hasStage(player, stage)) {
                    for (ItemStack item : player.getInventory().items) {
                        if (check(s, item, CHECK_TYPES.ALL)) {
                            dropItemStack(player, item);
                        }
                    }
                }
            });
            prevInventory = new HashSet<>(player.getInventory().items);
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void onItemTooltip(ItemTooltipEvent event) {
        Player player = Minecraft.getInstance().player;
        final IStagesData stageData = StageUtil.getPlayerData(player);
        if (stageData == null) return;
        final ArrayList<String> playerStages = stageData.getStages();
        Registry.getRestrictions().forEach((s, x) -> {
            String stage = RestrictionsData.getRestrictionData(s).getData().getStage().toLowerCase(Locale.ROOT).toLowerCase(Locale.ROOT);
            if (RestrictionsData.getRestrictionData(s).getSettingsCodec().getAdvancedTooltips().equals("NONE")) return;
            if ((RestrictionsData.getRestrictionData(s).getSettingsCodec().getAdvancedTooltips().equals("ADVANCED") && !playerStages.contains(stage) && event.getFlags().isAdvanced()) || !playerStages.contains(stage) && RestrictionsData.getRestrictionData(s).getSettingsCodec().getAdvancedTooltips().equals("ALWAYS")) {
                ItemStack itemStack = null;
                //Collect ingredients from mod list
                if (check(s, event.getItemStack(), CHECK_TYPES.ALL)) {
                    itemStack = event.getItemStack();
                }

                if (itemStack != null) {
                    if (!RestrictionsData.getRestrictionData(s).getSettingsCodec().getItemTitle().equals("")) {
                        event.getToolTip().set(0, Component.literal(RestrictionsData.getRestrictionData(s).getSettingsCodec().getItemTitle()));
                    }
                    event.getToolTip().add(Component.translatable("thitemstages.tooltip.stage.message").withStyle(ChatFormatting.DARK_PURPLE).withStyle(ChatFormatting.BOLD)
                            .append(Component.translatable("thitemstages.tooltip.stage.left_bracket").withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.DARK_PURPLE))
                            .append(Component.translatable("thitemstages.tooltip.stage.stage", stage).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.BOLD))
                            .append(Component.translatable("thitemstages.tooltip.stage.right_bracket").withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.DARK_PURPLE))
                    );
                    if (!RestrictionsData.getRestrictionData(s).getSettingsCodec().getCanPickup()) {
                        event.getToolTip().add(Component.translatable("thitemstages.tooltip.pickup").withStyle(ChatFormatting.WHITE));
                    }
                    if (RestrictionsData.getRestrictionData(s).getSettingsCodec().getCheckPlayerInventory()) {
                        event.getToolTip().add(Component.translatable("thitemstages.tooltip.playerinventory").withStyle(ChatFormatting.WHITE));
                    }
                    if (RestrictionsData.getRestrictionData(s).getSettingsCodec().getCheckPlayerEquipment()) {
                        event.getToolTip().add(Component.translatable("thitemstages.tooltip.playerequipment").withStyle(ChatFormatting.WHITE));
                    }
                    if (!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableItems()) {
                        event.getToolTip().add(Component.translatable("thitemstages.tooltip.usableitems").withStyle(ChatFormatting.WHITE));
                    }
                    if (!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableBlocks()) {
                        event.getToolTip().add(Component.translatable("thitemstages.tooltip.usableblocks").withStyle(ChatFormatting.WHITE));
                    }
                    if (RestrictionsData.getRestrictionData(s).getSettingsCodec().getHideInJEI()) {
                        event.getToolTip().add(Component.translatable("thitemstages.tooltip.hideinjei").withStyle(ChatFormatting.WHITE));
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public void onPlayerInteractWithBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntity() != null) {
            Player player = event.getEntity();
            Registry.getRestrictions().forEach((s, x) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage().toLowerCase(Locale.ROOT);

                if (!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableBlocks() && !hasStage(player, stage)) {
                    if (check(s, new ItemStack(event.getLevel().getBlockState(event.getPos()).getBlock().asItem()), CHECK_TYPES.ALL)) {
                        event.setCanceled(true);
                        player.displayClientMessage(Utils.BLOCK_INTERACT_ERROR, true);
                    }
                }
            });
        }
    }

    // credits to https://github.com/mangdags
    @SubscribeEvent
    public void onPlayerDestroyBlock(BlockEvent.BreakEvent event) {
        Registry.getRestrictions().forEach((s, x) -> {
            String stage = RestrictionsData.getRestrictionData(s).getData().getStage();

            if (!RestrictionsData.getRestrictionData(s).getSettingsCodec().getDestroyableBlocks() && !hasStage(event.getPlayer(), stage)) {
                if (check(s, new ItemStack(event.getLevel().getBlockState(event.getPos()).getBlock().asItem()), CHECK_TYPES.ALL)) {
                    event.setCanceled(true);
                    event.getPlayer().displayClientMessage(Utils.BLOCK_DESTROY_ERROR, true);
                }
            }
        });
    }

    @SubscribeEvent
    public void onPlayerInteractWithBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() != null) {
            Player player = event.getEntity();
            Registry.getRestrictions().forEach((s, x) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage().toLowerCase(Locale.ROOT);

                if (!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableBlocks() && !hasStage(player, stage)) {
                    if (check(s, new ItemStack(event.getLevel().getBlockState(event.getPos()).getBlock().asItem()), CHECK_TYPES.ALL)) {
                        player.displayClientMessage(Utils.BLOCK_INTERACT_WARN, true);
                    }
                }
                if (!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableItems() && !hasStage(player, stage)) {
                    if (check(s, event.getItemStack(), CHECK_TYPES.ALL)) {
                        event.setCanceled(true);
                        player.displayClientMessage(Utils.ITEM_INTERACT_ERROR, true);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void onPlayerInteractWithItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntity() != null) {
            Player player = event.getEntity();
            Registry.getRestrictions().forEach((s, x) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage().toLowerCase(Locale.ROOT);

                if (!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableItems() && !hasStage(player, stage)) {
                    if (check(s, event.getItemStack(), CHECK_TYPES.ALL)) {
                        event.setCanceled(true);
                        player.displayClientMessage(Utils.ITEM_INTERACT_ERROR, true);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(LivingAttackEvent event) {
        if (event.getSource() != null && event.getSource().getEntity() instanceof Player player && event.isCancelable() && event.getSource().getEntity() != null && !event.getSource().getEntity().level().isClientSide && !(event.getSource().getEntity() instanceof FakePlayer)) {
            Registry.getRestrictions().forEach((s, x) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage().toLowerCase(Locale.ROOT);

                if (!RestrictionsData.getRestrictionData(s).getSettingsCodec().getUsableItems() && !hasStage(player, stage)) {
                    if (check(s, player.getMainHandItem(), CHECK_TYPES.ALL)) {
                        event.setCanceled(true);
                        player.displayClientMessage(ITEM_INTERACT_ERROR, true);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public void onEntitySpawn(MobSpawnEvent.FinalizeSpawn event) {
        if (event.getSpawnType().equals(MobSpawnType.NATURAL)) {
            var ref = new Object() {
                Player closestPlayer = null;
            };
            for (ServerPlayer player : Objects.requireNonNull(event.getLevel().getServer()).getPlayerList().getPlayers()) {
                if (ref.closestPlayer == null)
                    ref.closestPlayer = player;
                if (player.distanceTo(event.getEntity()) < ref.closestPlayer.distanceTo(event.getEntity())) {
                    ref.closestPlayer = player;
                }
            }
            ResourceLocation entityID = ForgeRegistries.ENTITY_TYPES.getKey(event.getEntity().getType());
            String entityCategory = event.getEntity().getType().getCategory().getName();

            AtomicBoolean shouldSpawn = new AtomicBoolean(false);

            Registry.getRestrictions().forEach((s, x) -> {
                String stage = RestrictionsData.getRestrictionData(s).getData().getStage().toLowerCase(Locale.ROOT);
                List<MobRestriction> mobList = RestrictionsData.getRestrictionData(s).getData().getMobList();
                if (hasStage(ref.closestPlayer, stage)) {
                    mobList.forEach(e -> {
                        boolean matchesEntityID = e.getEntityID().isPresent() && e.getEntityID().get().equals(entityID);
                        boolean matchesMobCategory = e.getMobCategory().isPresent() && e.getMobCategory().get().equals(entityCategory);

                        if (matchesEntityID || matchesMobCategory) {
                            int lightLevel = event.getEntity().level().getLightEmission(event.getEntity().blockPosition());
                            shouldSpawn.set(true);
                            if(e.getMaxLight().isPresent() && e.getMaxLight().get() <= lightLevel)
                                shouldSpawn.set(false);
                            if(e.getMinLight().isPresent() && e.getMinLight().get() >=  lightLevel)
                                shouldSpawn.set(false);
                            if (e.getBiomes().isPresent()) {
                                boolean biomeMatch = e.getBiomes().get().stream()
                                        .anyMatch(resourceLocation -> event.getEntity().level().getBiome(event.getEntity().blockPosition()).is(resourceLocation));
                                if (!biomeMatch) {
                                    shouldSpawn.set(false);
                                }
                            }
                            if(e.getHealth().isPresent())
                                event.getEntity().setHealth(e.getHealth().get());
                            if (e.getEffects().isPresent()) {
                                for (EffectsCodec effect : e.getEffects().get()) {
                                    if (new Random().nextInt(100) < effect.getChance()) {
                                        ResourceLocation effectLocation = effect.getResourceLocation();
                                        int duration = effect.getDuration();
                                        if(effect.getDuration() == 0)
                                            duration = Integer.MAX_VALUE;
                                        int amplifier = effect.getAmplifier();

                                        MobEffect mobEffect = ForgeRegistries.MOB_EFFECTS.getValue(effectLocation);
                                        if (mobEffect != null) {
                                            event.getEntity().addEffect(new MobEffectInstance(mobEffect, duration, amplifier));
                                        } else {

                                            THItemStages.LOGGER.warn("Warning: Mob effect " + effectLocation + " not found in registry.");
                                        }
                                    }
                                }
                            }
                            if (!e.getLoadoutList().isEmpty()) {
                                e.getLoadoutList().forEach(item -> {
                                    if(new Random().nextInt(100) < item.getChance()) {
                                        if(item.getSlot().equals("MAINHAND")) {
                                            event.getEntity().setItemSlot(EquipmentSlot.MAINHAND, item.getItemStack());
                                        }
                                        if(item.getSlot().equals("OFFHAND")) {
                                            event.getEntity().setItemSlot(EquipmentSlot.OFFHAND, item.getItemStack());
                                        }
                                        if(item.getSlot().equals("FEET")) {
                                            event.getEntity().setItemSlot(EquipmentSlot.FEET, item.getItemStack());
                                        }
                                        if(item.getSlot().equals("LEGS")) {
                                            event.getEntity().setItemSlot(EquipmentSlot.LEGS, item.getItemStack());
                                        }
                                        if(item.getSlot().equals("CHEST")) {
                                            event.getEntity().setItemSlot(EquipmentSlot.CHEST, item.getItemStack());
                                        }
                                        if(item.getSlot().equals("HEAD")) {
                                            event.getEntity().setItemSlot(EquipmentSlot.HEAD, item.getItemStack());
                                        }
                                    }
                                });
                            }
                        }
                    });
                }
            });

            if (!shouldSpawn.get()) {
                event.setSpawnCancelled(true);
                event.setResult(Event.Result.DENY);
                event.setCanceled(true);
            }
            else {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onDebugOverlay(CustomizeGuiOverlayEvent.DebugText event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.renderDebug) {
            LocalPlayer player = mc.player;
            if (player != null) {
                HitResult hitResult = mc.hitResult;
                if (hitResult == null) {
                    hitResult = player.pick(20.0D, 0.0F, false);
                }

                if (hitResult.getType() == HitResult.Type.ENTITY) {
                    EntityHitResult entityHitResult = (EntityHitResult) hitResult;
                    Entity target = entityHitResult.getEntity();

                    if (target instanceof LivingEntity livingTarget) {
                        //event.getLeft().add("Entity ID: " + ForgeRegistries.ENTITY_TYPES.getKey(livingTarget.getType()));
                        event.getRight().add("Category: " + livingTarget.getType().getCategory().getName());
                    }
                }
            }
        }
    }
}