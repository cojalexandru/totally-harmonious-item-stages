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
import net.minecraft.command.arguments.ArgumentSerializer;
import net.minecraft.network.PacketBuffer;

import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class StageArgumentType implements ArgumentType<String> {

    public static final ArgumentSerializer<StageArgumentType> SERIALIZER = new Serializer();
    private Set<String> KNOWN_STAGES = null;

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

    static class Serializer extends ArgumentSerializer<StageArgumentType> {

        private Serializer() {
            super(StageArgumentType::new);
        }

        @Override
        public void serializeToNetwork (StageArgumentType arg, PacketBuffer buffer) {
            buffer.writeUtf(arg.KNOWN_STAGES.toString());
        }

        @Override
        public StageArgumentType deserializeFromNetwork (PacketBuffer buffer) {
            final StageArgumentType argType = super.deserializeFromNetwork(buffer);
            argType.KNOWN_STAGES = Arrays.stream(buffer.readUtf().replaceAll("\\s", "").replaceAll("[\\[\\]]", "").split(",")).collect(Collectors.toSet());
            return argType;
        }
    }
}