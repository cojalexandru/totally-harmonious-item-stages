package com.decursioteam.thitemstages.commands;

import com.decursioteam.thitemstages.Registry;
import com.decursioteam.thitemstages.THItemStages;
import com.decursioteam.thitemstages.datagen.RestrictionsData;
import com.decursioteam.thitemstages.datagen.utils.FileUtils;
import com.decursioteam.thitemstages.utils.StageUtil;
import com.decursioteam.thitemstages.utils.StagesHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import net.minecraft.commands.synchronization.SingletonArgumentInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.decursioteam.thitemstages.datagen.utils.FileUtils.restrictionExists;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class THISCommands {

    private static final DeferredRegister<ArgumentTypeInfo<?, ?>> COMMAND_ARGUMENT_TYPES = DeferredRegister.create(net.minecraft.core.Registry.COMMAND_ARGUMENT_TYPE_REGISTRY, THItemStages.MOD_ID);
    private static final RegistryObject<SingletonArgumentInfo<StageArgumentType>> STAGE_ARGUMENT = COMMAND_ARGUMENT_TYPES.register("stage_argument", () -> ArgumentTypeInfos.registerByClass(StageArgumentType.class, SingletonArgumentInfo.contextFree(StageArgumentType::new)));
    private static final RegistryObject<SingletonArgumentInfo<RestrictionArgumentType>> RESTRICTION_ARGUMENT = COMMAND_ARGUMENT_TYPES.register("restriction_argument", () -> ArgumentTypeInfos.registerByClass(RestrictionArgumentType.class, SingletonArgumentInfo.contextFree(RestrictionArgumentType::new)));

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(THISCommands::registerCommands);
        COMMAND_ARGUMENT_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        final LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("thitemstages");
        command.then(createSilentStageCommand("add", 2, ctx -> addStage(ctx, false), ctx -> addStage(ctx, true)));
        command.then(createSilentStageCommand("remove", 2, ctx -> removeStage(ctx, false), ctx -> removeStage(ctx, true)));
        command.then(createPlayerCommand("check", 0, ctx -> getPlayerStages(ctx, true), ctx -> getPlayerStages(ctx, false)));
        command.then(createPlayerCommand("clear", 2, ctx -> clearStages(ctx, true), ctx -> clearStages(ctx, false)));
        command.then(createPlayerCommand("all", 2, ctx -> giveStages(ctx, true), ctx -> giveStages(ctx, false)));
        command.then(createRestrictCommand("advanced_restrict", 2, THISCommands::restrictItem));
        //command.then(createFileRestrictCommand("restrict", 2, THISCommands::restrictItemWithFile));
        command.then(createInfoCommand("reload", 2, THISCommands::reloadStages));
        command.then(createInfoCommand("info", 2, THISCommands::listStages));
        event.getDispatcher().register(command);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createInfoCommand (String key, int permissions, Command<CommandSourceStack> command) {
        return Commands.literal(key).requires(sender -> sender.hasPermission(permissions)).executes(command);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createPlayerCommand (String key, int permissions, Command<CommandSourceStack> command, Command<CommandSourceStack> commandNoPlayer) {
        return Commands.literal(key).requires(sender -> sender.hasPermission(permissions)).executes(commandNoPlayer).then(Commands.argument("targets", EntityArgument.player()).executes(command));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createRestrictCommand (String key, int permissions, Command<CommandSourceStack> command) {
        return Commands.literal(key).requires(sender -> sender.hasPermission(permissions))
                .then(Commands.argument("stage", new StageArgumentType())
                        .then(Commands.argument("advancedTooltips", StringArgumentType.string())
                        .then(Commands.argument("itemTitle", StringArgumentType.string())
                        .then(Commands.argument("pickupDelay", IntegerArgumentType.integer())
                        .then(Commands.argument("hideInJEI", BoolArgumentType.bool())
                        .then(Commands.argument("canPickup", BoolArgumentType.bool())
                        .then(Commands.argument("containerListWhitelist", BoolArgumentType.bool())
                        .then(Commands.argument("checkPlayerInventory", BoolArgumentType.bool())
                        .then(Commands.argument("checkPlayerEquipment", BoolArgumentType.bool())
                        .then(Commands.argument("usableItems", BoolArgumentType.bool())
                        .then(Commands.argument("usableBlocks", BoolArgumentType.bool())
                        .then(Commands.argument("destroyableBlocks", BoolArgumentType.bool())
                        .executes(command)))))))))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createFileRestrictCommand (String key, int permissions, Command<CommandSourceStack> command) {
        return Commands.literal(key).requires(sender -> sender.hasPermission(permissions))
                .then(Commands.argument("stage", new StageArgumentType())
                .then(Commands.argument("restriction", new RestrictionArgumentType())
                .executes(command)));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> createSilentStageCommand (String key, int permissions, Command<CommandSourceStack> command, Command<CommandSourceStack> silent) {
        return Commands.literal(key).requires(sender -> sender.hasPermission(permissions))
                .then(Commands.argument("targets", EntityArgument.players())
                        .then(Commands.argument("stage", new StageArgumentType())
                                .executes(command).then(Commands.argument("silent", BoolArgumentType.bool())
                                        .executes(silent))));
    }

    private static int reloadStages (CommandContext<CommandSourceStack> ctx) {
        Registry.setupRestrictions();
        Registry.registerRestrictionsList();
        ctx.getSource().sendSuccess(Component.translatable("thitemstages.commands.reloadstages", StagesHandler.getStages()), true);
        return 0;
    }

    private static int listStages (CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(Component.translatable("thitemstages.commands.liststages", StagesHandler.getStages()), true);
        return 0;
    }


    private static int addStage(CommandContext<CommandSourceStack> ctx, boolean silent) throws CommandSyntaxException {
        final String stage = StageArgumentType.getStage(ctx, "stage");
        for (final ServerPlayer player : EntityArgument.getPlayers(ctx, "targets")) {
            if(stage.contains(",")){
                Set<String> stages = Arrays.stream(stage.split(",")).collect(Collectors.toSet());
                for (String s : stages) {
                    if(!StageUtil.hasStage(player, s)) StageUtil.addStage(player, s);
                }
                if (!silent || !BoolArgumentType.getBool(ctx, "silent")) {
                    ctx.getSource().sendSuccess(Component.translatable("thitemstages.commands.addstages.success.target", stages), true);
                    if (player != ctx.getSource().getEntity()) {
                        ctx.getSource().sendSuccess(Component.translatable("thitemstages.commands.addstages.success.sender", player.getDisplayName(), stages), true);
                    }
                }
            }
            else {
                if(!StageUtil.hasStage(player, stage)) StageUtil.addStage(player, stage);
                else if (!silent || !BoolArgumentType.getBool(ctx, "silent")) {
                    ctx.getSource().sendFailure(Component.translatable("thitemstages.commands.addstage.failure.target", stage));
                    if (player != ctx.getSource().getEntity()) {
                        ctx.getSource().sendFailure(Component.translatable("thitemstages.commands.addstage.failure.sender", player.getDisplayName(), stage));
                    }
                    return 0;
                }
                if (!silent || !BoolArgumentType.getBool(ctx, "silent")) {
                    ctx.getSource().sendSuccess(Component.translatable("thitemstages.commands.addstage.success.target", stage), true);
                    if (player != ctx.getSource().getEntity()) {
                        ctx.getSource().sendSuccess(Component.translatable("thitemstages.commands.addstage.success.sender", player.getDisplayName(), stage), true);
                    }
                }
            }
        }
        return 0;
    }

    private static int removeStage(CommandContext<CommandSourceStack> ctx, boolean silent) throws CommandSyntaxException {
        final String stage = StageArgumentType.getStage(ctx, "stage");
        for (final ServerPlayer player : EntityArgument.getPlayers(ctx, "targets")) {
            if(stage.contains(",")){
                Set<String> stages = Arrays.stream(stage.split(",")).collect(Collectors.toSet());
                for (String s : stages) {
                    if(StageUtil.hasStage(player, s)) StageUtil.removeStage(player, s);
                }
                if (!silent || !BoolArgumentType.getBool(ctx, "silent")) {
                    ctx.getSource().sendSuccess(Component.translatable("thitemstages.commands.removestages.success.target", stages), true);
                    if (player != ctx.getSource().getEntity()) {
                        ctx.getSource().sendSuccess(Component.translatable("thitemstages.commands.removestages.success.sender", player.getDisplayName(), stages), true);
                    }
                }
            }
            else {
                if(StageUtil.hasStage(player, stage)) StageUtil.removeStage(player, stage);
                else if (!silent || !BoolArgumentType.getBool(ctx, "silent")) {
                    ctx.getSource().sendFailure(Component.translatable("thitemstages.commands.removestage.failure.target", stage));
                    if (player != ctx.getSource().getEntity()) {
                        ctx.getSource().sendFailure(Component.translatable("thitemstages.commands.removestage.failure.sender", player.getDisplayName(), stage));
                    }
                    return 0;
                }
                if (!silent || !BoolArgumentType.getBool(ctx, "silent")) {
                    ctx.getSource().sendSuccess(Component.translatable("thitemstages.commands.removestage.success.target", stage), true);
                    if (player != ctx.getSource().getEntity()) {
                        ctx.getSource().sendSuccess(Component.translatable("thitemstages.commands.removestage.success.sender", player.getDisplayName(), stage), true);
                    }
                }
            }
        }
        return 0;
    }

    private static int getPlayerStages(CommandContext<CommandSourceStack> ctx, boolean hasPlayer) throws CommandSyntaxException {
        if (hasPlayer) {
            for (final ServerPlayer player : EntityArgument.getPlayers(ctx, "targets")) {
                getPlayerStages(ctx, player);
            }
        }
        else {
            getPlayerStages(ctx, ctx.getSource().getPlayerOrException());
        }
        return 0;
    }

    private static void getPlayerStages (CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        final String stageInfo = Objects.requireNonNull(StageUtil.getPlayerData(player)).getStages().stream().map(Object::toString).collect(Collectors.joining(", "));
        if (stageInfo.isEmpty()) ctx.getSource().sendFailure(Component.translatable("thitemstages.commands.check.failure.empty", player.getDisplayName()));
        else ctx.getSource().sendSuccess(Component.translatable("thitemstages.commands.check.success.list", player.getDisplayName(), stageInfo), false);
    }

    private static int giveStages(CommandContext<CommandSourceStack> ctx, boolean hasPlayer) throws CommandSyntaxException {
        if (hasPlayer)
            for (final ServerPlayer player : EntityArgument.getPlayers(ctx, "targets")) giveStages(ctx, player);
        else giveStages(ctx, ctx.getSource().getPlayerOrException());
        return 0;
    }

    private static Path createCustomPath(String pathName) {
        Path customPath = Paths.get(FMLPaths.CONFIGDIR.get().toAbsolutePath().toString(), THItemStages.MOD_ID, pathName);
        createDirectory(customPath, pathName);
        return customPath;
    }
    private static void createDirectory(Path path, String dirName) {
        try {
            Files.createDirectories(path);
        } catch (FileAlreadyExistsException ignored) {
        } catch (IOException e) {
            THItemStages.LOGGER.error("failed to create \"{}\" directory", dirName);
        }
    }

    private static void giveStages(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        for(final String knownStage : StageUtil.getStages()) StageUtil.addStage(player, knownStage);
        ctx.getSource().sendSuccess(Component.translatable("thitemstages.commands.all.target"), true);
        if(player != ctx.getSource().getEntity()) ctx.getSource().sendSuccess(Component.translatable("thitemstages.commands.all.sender", player.getDisplayName()), true);
    }

    private static int clearStages(CommandContext<CommandSourceStack> ctx, boolean hasPlayer) throws CommandSyntaxException {
        if(hasPlayer) for (final ServerPlayer player : EntityArgument.getPlayers(ctx, "targets")) clearStages(ctx, player);
        else clearStages(ctx, ctx.getSource().getPlayerOrException());
        return 0;
    }

    private static void clearStages(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        final int removedStages = StageUtil.clearStages(player);
        ctx.getSource().sendSuccess(Component.translatable("thitemstages.commands.clear.target", removedStages), true);
        if(player != ctx.getSource().getEntity()) ctx.getSource().sendSuccess(Component.translatable("thitemstages.commands.clear.sender", removedStages, player.getDisplayName()), true);
    }

    private static int restrictItem(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final String stage = StageArgumentType.getStage(ctx, "stage");
        final String advancedTooltips = StringArgumentType.getString(ctx, "advancedTooltips");
        final String itemTitle = StringArgumentType.getString(ctx, "itemTitle");
        final int pickupDelay = IntegerArgumentType.getInteger(ctx, "pickupDelay");
        final boolean hideInJEI = BoolArgumentType.getBool(ctx, "hideInJEI");
        final boolean canPickup = BoolArgumentType.getBool(ctx, "canPickup");
        final boolean containerListWhitelist = BoolArgumentType.getBool(ctx, "containerListWhitelist");
        final boolean checkPlayerInventory = BoolArgumentType.getBool(ctx, "checkPlayerInventory");
        final boolean checkPlayerEquipment = BoolArgumentType.getBool(ctx, "checkPlayerEquipment");
        final boolean usableItems = BoolArgumentType.getBool(ctx, "usableItems");
        final boolean usableBlocks = BoolArgumentType.getBool(ctx, "usableBlocks");
        final boolean destroyableBlocks = BoolArgumentType.getBool(ctx, "destroyableBlocks");

        Player player = ctx.getSource().getPlayerOrException();
        AtomicInteger counter = new AtomicInteger();
        RestrictionsData.getRegistry().getRawRestrictions().forEach((restriction, x) -> {
            if(restrictionExists(restriction, stage, advancedTooltips, itemTitle, pickupDelay, hideInJEI, canPickup, containerListWhitelist, checkPlayerInventory, checkPlayerEquipment, usableItems, usableBlocks, destroyableBlocks))
            {
                FileUtils.restrictItem(stage, advancedTooltips, itemTitle, pickupDelay, hideInJEI, canPickup, containerListWhitelist, checkPlayerInventory, checkPlayerEquipment, usableItems, usableBlocks, destroyableBlocks, player.getItemInHand(InteractionHand.MAIN_HAND));
                counter.getAndIncrement();
            }
        });
        if(!(counter.get() > 0)){
            FileUtils.addRestriction(stage, advancedTooltips, itemTitle, pickupDelay, hideInJEI, canPickup, containerListWhitelist, checkPlayerInventory, checkPlayerEquipment, usableItems, usableBlocks, destroyableBlocks);
            FileUtils.restrictItem(stage, advancedTooltips, itemTitle, pickupDelay, hideInJEI, canPickup, containerListWhitelist, checkPlayerInventory, checkPlayerEquipment, usableItems, usableBlocks, destroyableBlocks, player.getItemInHand(InteractionHand.MAIN_HAND));
        }
        return 0;
    }

}
