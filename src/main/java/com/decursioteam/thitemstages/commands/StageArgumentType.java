package com.decursioteam.thitemstages.commands;

import com.decursioteam.thitemstages.utils.StageUtil;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class StageArgumentType implements ArgumentType<String> {
    private static Set<String> KNOWN_STAGES = null;

    public StageArgumentType() {
        KNOWN_STAGES = StageUtil.getStages();
    }

    public static String getStage(CommandContext<CommandSource> ctx, String name) throws CommandSyntaxException {
        return ctx.getArgument(name, String.class);
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    @Override
    public String toString () {
        return "stage";
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> ctx, SuggestionsBuilder builder) {
        return ISuggestionProvider.suggest(KNOWN_STAGES, builder);
    }
}