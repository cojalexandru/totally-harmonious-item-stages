package com.decursioteam.thitemstages.commands;

import com.decursioteam.thitemstages.utils.StageUtil;
import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;

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

    public static String getStage(CommandContext<CommandSourceStack> ctx, String name) throws CommandSyntaxException {
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
        return SharedSuggestionProvider.suggest(KNOWN_STAGES, builder);
    }

    static class Serializer implements ArgumentSerializer<StageArgumentType> {

        @Override
        public void serializeToNetwork (StageArgumentType arg, FriendlyByteBuf buffer) {
            buffer.writeUtf(arg.KNOWN_STAGES.toString());
        }

        @Override
        public StageArgumentType deserializeFromNetwork (FriendlyByteBuf buffer) {
            final StageArgumentType argType = new StageArgumentType();
            argType.KNOWN_STAGES = Arrays.stream(buffer.readUtf().replaceAll("\\s", "").replaceAll("[\\[\\]]", "").split(",")).collect(Collectors.toSet());
            return argType;
        }

        @Override
        public void serializeToJson(StageArgumentType p_121577_, JsonObject p_121578_) {
            p_121578_.add("known_stages", GsonHelper.parse(p_121577_.KNOWN_STAGES.toString()));
        }
    }
}