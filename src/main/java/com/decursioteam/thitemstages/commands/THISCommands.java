package com.decursioteam.thitemstages.commands;

import com.decursioteam.thitemstages.Registry;
import com.decursioteam.thitemstages.datagen.RestrictionsData;
import com.decursioteam.thitemstages.utils.StageUtil;
import com.decursioteam.thitemstages.utils.StagesHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.EntityArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class THISCommands {

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(THISCommands::registerCommands);
    }

    private static void registerCommands(RegisterCommandsEvent event) {
        final LiteralArgumentBuilder<CommandSource> command = Commands.literal("thitemstages");
        command.then(createSilentStageCommand("add", 2, ctx -> addStage(ctx, false), ctx -> addStage(ctx, true)));
        command.then(createSilentStageCommand("remove", 2, ctx -> removeStage(ctx, false), ctx -> removeStage(ctx, true)));
        command.then(createPlayerCommand("check", 0, ctx -> getPlayerStages(ctx, true), ctx -> getPlayerStages(ctx, false)));
        command.then(createPlayerCommand("clear", 2, ctx -> clearStages(ctx, true), ctx -> clearStages(ctx, false)));
        command.then(createPlayerCommand("all", 2, ctx -> giveStages(ctx, true), ctx -> giveStages(ctx, false)));
        command.then(createInfoCommand("reload", 2, THISCommands::reloadStages));
        command.then(createInfoCommand("info", 2, THISCommands::listStages));
        event.getDispatcher().register(command);
    }

    private static LiteralArgumentBuilder<CommandSource> createInfoCommand (String key, int permissions, Command<CommandSource> command) {
        return Commands.literal(key).requires(sender -> sender.hasPermission(permissions)).executes(command);
    }

    private static LiteralArgumentBuilder<CommandSource> createPlayerCommand (String key, int permissions, Command<CommandSource> command, Command<CommandSource> commandNoPlayer) {
        return Commands.literal(key).requires(sender -> sender.hasPermission(permissions)).executes(commandNoPlayer).then(Commands.argument("targets", EntityArgument.player()).executes(command));
    }

    private static LiteralArgumentBuilder<CommandSource> createSilentStageCommand (String key, int permissions, Command<CommandSource> command, Command<CommandSource> silent) {
        return Commands.literal(key).requires(sender -> sender.hasPermission(permissions)).then(Commands.argument("targets", EntityArgument.players()).then(Commands.argument("stage", new StageArgumentType()).executes(command).then(Commands.argument("silent", BoolArgumentType.bool()).executes(silent))));
    }

    private static int reloadStages (CommandContext<CommandSource> ctx) {
        Registry.setupRestrictions();
        Registry.registerRestrictionsList();
        ctx.getSource().sendSuccess(new TranslationTextComponent("thitemstages.commands.reloadstages", StagesHandler.getStages()), true);
        return 0;
    }

    private static int listStages (CommandContext<CommandSource> ctx) {
        ctx.getSource().sendSuccess(new TranslationTextComponent("thitemstages.commands.liststages", StagesHandler.getStages()), true);
        return 0;
    }


    private static int addStage(CommandContext<CommandSource> ctx, boolean silent) throws CommandSyntaxException {
        final String stage = StageArgumentType.getStage(ctx, "stage");
        for (final ServerPlayerEntity player : EntityArgument.getPlayers(ctx, "targets")) {
            if(stage.contains(",")){
                Set<String> stages = Arrays.stream(stage.split(",")).collect(Collectors.toSet());
                for (String s : stages) {
                    if(!StageUtil.hasStage(player, s)) StageUtil.addStage(player, s);
                }
                if (!silent || !BoolArgumentType.getBool(ctx, "silent")) {
                    ctx.getSource().sendSuccess(new TranslationTextComponent("thitemstages.commands.addstages.success.target", stages), true);
                    if (player != ctx.getSource().getEntity()) {
                        ctx.getSource().sendSuccess(new TranslationTextComponent("thitemstages.commands.addstages.success.sender", player.getDisplayName(), stages), true);
                    }
                }
            }
            else {
                if(!StageUtil.hasStage(player, stage)) StageUtil.addStage(player, stage);
                else if (!silent || !BoolArgumentType.getBool(ctx, "silent")) {
                    ctx.getSource().sendFailure(new TranslationTextComponent("thitemstages.commands.addstage.failure.target", stage));
                    if (player != ctx.getSource().getEntity()) {
                        ctx.getSource().sendFailure(new TranslationTextComponent("thitemstages.commands.addstage.failure.sender", player.getDisplayName(), stage));
                    }
                    return 0;
                }
                if (!silent || !BoolArgumentType.getBool(ctx, "silent")) {
                    ctx.getSource().sendSuccess(new TranslationTextComponent("thitemstages.commands.addstage.success.target", stage), true);
                    if (player != ctx.getSource().getEntity()) {
                        ctx.getSource().sendSuccess(new TranslationTextComponent("thitemstages.commands.addstage.success.sender", player.getDisplayName(), stage), true);
                    }
                }
            }
        }
        return 0;
    }

    private static int removeStage(CommandContext<CommandSource> ctx, boolean silent) throws CommandSyntaxException {
        final String stage = StageArgumentType.getStage(ctx, "stage");
        for (final ServerPlayerEntity player : EntityArgument.getPlayers(ctx, "targets")) {
            if(stage.contains(",")){
                Set<String> stages = Arrays.stream(stage.split(",")).collect(Collectors.toSet());
                for (String s : stages) {
                    if(StageUtil.hasStage(player, s)) StageUtil.removeStage(player, s);
                }
                if (!silent || !BoolArgumentType.getBool(ctx, "silent")) {
                    ctx.getSource().sendSuccess(new TranslationTextComponent("thitemstages.commands.removestages.success.target", stages), true);
                    if (player != ctx.getSource().getEntity()) {
                        ctx.getSource().sendSuccess(new TranslationTextComponent("thitemstages.commands.removestages.success.sender", player.getDisplayName(), stages), true);
                    }
                }
            }
            else {
                if(StageUtil.hasStage(player, stage)) StageUtil.removeStage(player, stage);
                else if (!silent || !BoolArgumentType.getBool(ctx, "silent")) {
                    ctx.getSource().sendFailure(new TranslationTextComponent("thitemstages.commands.removestage.failure.target", stage));
                    if (player != ctx.getSource().getEntity()) {
                        ctx.getSource().sendFailure(new TranslationTextComponent("thitemstages.commands.removestage.failure.sender", player.getDisplayName(), stage));
                    }
                    return 0;
                }
                if (!silent || !BoolArgumentType.getBool(ctx, "silent")) {
                    ctx.getSource().sendSuccess(new TranslationTextComponent("thitemstages.commands.removestage.success.target", stage), true);
                    if (player != ctx.getSource().getEntity()) {
                        ctx.getSource().sendSuccess(new TranslationTextComponent("thitemstages.commands.removestage.success.sender", player.getDisplayName(), stage), true);
                    }
                }
            }
        }
        return 0;
    }

    private static int getPlayerStages (CommandContext<CommandSource> ctx, boolean hasPlayer) throws CommandSyntaxException {
        if (hasPlayer) {
            for (final ServerPlayerEntity player : EntityArgument.getPlayers(ctx, "targets")) {
                getPlayerStages(ctx, player);
            }
        }
        else {
            getPlayerStages(ctx, ctx.getSource().getPlayerOrException());
        }
        return 0;
    }

    private static void getPlayerStages (CommandContext<CommandSource> ctx, ServerPlayerEntity player) {
        final String stageInfo = Objects.requireNonNull(StageUtil.getPlayerData(player)).getStages().stream().map(Object::toString).collect(Collectors.joining(", "));
        if (stageInfo.isEmpty()) ctx.getSource().sendFailure(new TranslationTextComponent("thitemstages.commands.check.failure.empty", player.getDisplayName()));
        else ctx.getSource().sendSuccess(new TranslationTextComponent("thitemstages.commands.check.success.list", player.getDisplayName(), stageInfo), false);
    }

    private static int giveStages(CommandContext<CommandSource> ctx, boolean hasPlayer) throws CommandSyntaxException {
        if(hasPlayer) for (final ServerPlayerEntity player : EntityArgument.getPlayers(ctx, "targets")) giveStages(ctx, player);
        else giveStages(ctx, ctx.getSource().getPlayerOrException());
        return 0;
    }

    private static void giveStages(CommandContext<CommandSource> ctx, ServerPlayerEntity player) {
        for(final String knownStage : StageUtil.getStages()) StageUtil.addStage(player, knownStage);
        ctx.getSource().sendSuccess(new TranslationTextComponent("thitemstages.commands.all.target"), true);
        if(player != ctx.getSource().getEntity()) ctx.getSource().sendSuccess(new TranslationTextComponent("thitemstages.commands.all.sender", player.getDisplayName()), true);
    }

    private static int clearStages(CommandContext<CommandSource> ctx, boolean hasPlayer) throws CommandSyntaxException {
        if(hasPlayer) for (final ServerPlayerEntity player : EntityArgument.getPlayers(ctx, "targets")) clearStages(ctx, player);
        else clearStages(ctx, ctx.getSource().getPlayerOrException());
        return 0;
    }

    private static void clearStages(CommandContext<CommandSource> ctx, ServerPlayerEntity player) {
        final int removedStages = StageUtil.clearStages(player);
        ctx.getSource().sendSuccess(new TranslationTextComponent("thitemstages.commands.clear.target", removedStages), true);
        if(player != ctx.getSource().getEntity()) ctx.getSource().sendSuccess(new TranslationTextComponent("thitemstages.commands.clear.sender", removedStages, player.getDisplayName()), true);
    }

}
