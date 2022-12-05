package com.decursioteam.thitemstages.utils;

import com.decursioteam.thitemstages.Registry;
import com.decursioteam.thitemstages.THItemStages;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class StagesReload implements PreparableReloadListener {

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier preparationBarrier, ResourceManager resourceManager, ProfilerFiller profilerFiller, ProfilerFiller profilerFiller1, Executor executor, Executor executor1) {
        return CompletableFuture.completedFuture(null).thenCompose(preparationBarrier::wait).thenAcceptAsync(x -> {
            Registry.setupRestrictions();
            Registry.registerRestrictionsList();
            THItemStages.LOGGER.info(new TranslatableContents("thitemstages.commands.reloadstages", StagesHandler.getStages()));
        }, executor);
    }
}
