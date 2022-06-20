package com.decursioteam.thitemstages.commands;

import com.decursioteam.thitemstages.Registry;
import com.decursioteam.thitemstages.utils.StageUtil;
import com.decursioteam.thitemstages.utils.StagesHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.synchronization.ArgumentTypes;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class THISCommands {

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(THISCommands::registerCommands);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(THISCommands::registerArguments);
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        final LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("thitemstages");
        command.then(createSilentStageCommand("add", 2, ctx -> addStage(ctx, false), ctx -> addStage(ctx, true)));
        command.then(createSilentStageCommand("remove", 2, ctx -> removeStage(ctx, false), ctx -> removeStage(ctx, true)));
        command.then(createPlayerCommand("check", 0, ctx -> getPlayerStages(ctx, true), ctx -> getPlayerStages(ctx, false)));
        command.then(createPlayerCommand("clear", 2, ctx -> clearStages(ctx, true), ctx -> clearStages(ctx, false)));
        command.then(createPlayerCommand("all", 2, ctx -> giveStages(ctx, true), ctx -> giveStages(ctx, false)));
        command.then(createInfoCommand("reload", 2, THISCommands::reloadStages));
        command.then(createInfoCommand("info", 2, THISCommands::listStages));
        event.getDispatcher().register(command);
    }

    private static void registerArguments(FMLCommonSetupEvent EVENT) {
        ArgumentTypes.register("stage_name", StageArgumentType.class, StageArgumentType.SERIALIZER);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> createInfoCommand (String key, int permissions, Command<CommandSourceStack> command) {
        return Commands.literal(key).requires(sender -> sender.hasPermission(permissions)).executes(command);
    }

    private static ArgumentBuilder<CommandSourceStack, ?> createPlayerCommand (String key, int permissions, Command<CommandSourceStack> command, Command<CommandSourceStack> commandNoPlayer) {
        return Commands.literal(key).requires(sender -> sender.hasPermission(permissions)).executes(commandNoPlayer).then(Commands.argument("targets", EntityArgument.player()).executes(command));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> createSilentStageCommand (String key, int permissions, Command<CommandSourceStack> command, Command<CommandSourceStack> silent) {
        return Commands.literal(key).requires(sender -> sender.hasPermission(permissions)).then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("stage", new StageArgumentType()).executes(command).then(Commands.argument("silent", BoolArgumentType.bool()).executes(silent))));
    }

    private static int reloadStages (CommandContext<CommandSourceStack> ctx) {
        Registry.setupRestrictions();
        Registry.registerRestrictionsList();
        ctx.getSource().sendSuccess(new TranslatableComponent("thitemstages.commands.reloadstages", StagesHandler.getStages()), true);
        return 0;
    }

    private static int listStages (CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendSuccess(new TranslatableComponent("thitemstages.commands.liststages", StagesHandler.getStages()), true);
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
                    ctx.getSource().sendSuccess(new TranslatableComponent("thitemstages.commands.addstages.success.target", stages), true);
                    if (player != ctx.getSource().getEntity()) {
                        ctx.getSource().sendSuccess(new TranslatableComponent("thitemstages.commands.addstages.success.sender", player.getDisplayName(), stages), true);
                    }
                }
            }
            else {
                if(!StageUtil.hasStage(player, stage)) StageUtil.addStage(player, stage);
                else if (!silent || !BoolArgumentType.getBool(ctx, "silent")) {
                    ctx.getSource().sendFailure(new TranslatableComponent("thitemstages.commands.addstage.failure.target"));
                    if (player != ctx.getSource().getEntity()) {
                        ctx.getSource().sendFailure(new TranslatableComponent("thitemstages.commands.addstage.failure.sender", player.getDisplayName(), stage));
                    }
                    return 0;
                }
                if (!silent || !BoolArgumentType.getBool(ctx, "silent")) {
                    ctx.getSource().sendSuccess(new TranslatableComponent("thitemstages.commands.addstage.success.target", stage), true);
                    if (player != ctx.getSource().getEntity()) {
                        ctx.getSource().sendSuccess(new TranslatableComponent("thitemstages.commands.addstage.success.sender", player.getDisplayName(), stage), true);
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
                    ctx.getSource().sendSuccess(new TranslatableComponent("thitemstages.commands.removestages.success.target", stages), true);
                    if (player != ctx.getSource().getEntity()) {
                        ctx.getSource().sendSuccess(new TranslatableComponent("thitemstages.commands.removestages.success.sender", player.getDisplayName(), stages), true);
                    }
                }
            }
            else {
                if(StageUtil.hasStage(player, stage)) StageUtil.removeStage(player, stage);
                else if (!silent || !BoolArgumentType.getBool(ctx, "silent")) {
                    ctx.getSource().sendFailure(new TranslatableComponent("thitemstages.commands.removestage.failure.target"));
                    if (player != ctx.getSource().getEntity()) {
                        ctx.getSource().sendFailure(new TranslatableComponent("thitemstages.commands.removestage.failure.sender", player.getDisplayName(), stage));
                    }
                    return 0;
                }
                if (!silent || !BoolArgumentType.getBool(ctx, "silent")) {
                    ctx.getSource().sendSuccess(new TranslatableComponent("thitemstages.commands.removestage.success.target", stage), true);
                    if (player != ctx.getSource().getEntity()) {
                        ctx.getSource().sendSuccess(new TranslatableComponent("thitemstages.commands.removestage.success.sender", player.getDisplayName(), stage), true);
                    }
                }
            }
        }
        return 0;
    }

    private static int getPlayerStages (CommandContext<CommandSourceStack> ctx, boolean hasPlayer) throws CommandSyntaxException {
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
        if (stageInfo.isEmpty()) ctx.getSource().sendFailure(new TranslatableComponent("thitemstages.commands.check.failure.empty", player.getDisplayName()));
        else ctx.getSource().sendSuccess(new TranslatableComponent("thitemstages.commands.check.success.list", player.getDisplayName(), stageInfo), false);
    }

    private static int giveStages(CommandContext<CommandSourceStack> ctx, boolean hasPlayer) throws CommandSyntaxException {
        if(hasPlayer) for (final ServerPlayer player : EntityArgument.getPlayers(ctx, "targets")) giveStages(ctx, player);
        else giveStages(ctx, ctx.getSource().getPlayerOrException());
        return 0;
    }

    private static void giveStages(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        for(final String knownStage : StageUtil.getStages()) StageUtil.addStage(player, knownStage);
        ctx.getSource().sendSuccess(new TranslatableComponent("thitemstages.commands.all.target"), true);
        if(player != ctx.getSource().getEntity()) ctx.getSource().sendSuccess(new TranslatableComponent("thitemstages.commands.all.sender", player.getDisplayName()), true);
    }

    private static int clearStages(CommandContext<CommandSourceStack> ctx, boolean hasPlayer) throws CommandSyntaxException {
        if(hasPlayer) for (final ServerPlayer player : EntityArgument.getPlayers(ctx, "targets")) clearStages(ctx, player);
        else clearStages(ctx, ctx.getSource().getPlayerOrException());
        return 0;
    }

    private static void clearStages(CommandContext<CommandSourceStack> ctx, ServerPlayer player) {
        final int removedStages = StageUtil.clearStages(player);
        ctx.getSource().sendSuccess(new TranslatableComponent("thitemstages.commands.clear.target"), true);
        if(player != ctx.getSource().getEntity()) ctx.getSource().sendSuccess(new TranslatableComponent("thitemstages.commands.clear.sender", removedStages, player.getDisplayName()), true);
    }

}
