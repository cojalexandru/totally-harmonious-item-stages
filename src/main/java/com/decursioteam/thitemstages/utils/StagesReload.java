package com.decursioteam.thitemstages.utils;

import com.decursioteam.thitemstages.Registry;
import com.decursioteam.thitemstages.THItemStages;
import com.decursioteam.thitemstages.datagen.RestrictionsData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class StagesReload implements PreparableReloadListener {

    @Override
    public @NotNull CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profilerFiller, @NotNull ProfilerFiller profilerFiller1, @NotNull Executor executor, @NotNull Executor executor1) {
        return CompletableFuture.completedFuture(null).thenCompose(preparationBarrier::wait).thenAcceptAsync(x -> {
            RestrictionsData.getRegistry().clearRawRestrictionsData();
            Registry.setupRestrictions();
            Registry.registerRestrictionsList();
            THItemStages.LOGGER.info(Component.translatable("thitemstages.commands.reloadstages", StagesHandler.getStages()).toString());
        }, executor);
    }
}
