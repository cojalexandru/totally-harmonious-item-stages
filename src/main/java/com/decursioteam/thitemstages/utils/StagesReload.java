package com.decursioteam.thitemstages.utils;

import com.decursioteam.thitemstages.Registry;
import com.decursioteam.thitemstages.THItemStages;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class StagesReload implements PreparableReloadListener {

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier p_10638_, ResourceManager p_10639_, ProfilerFiller p_10640_, ProfilerFiller p_10641_, Executor p_10642_, Executor p_10643_) {
        return CompletableFuture.completedFuture(null).thenCompose(p_10638_::wait).thenAcceptAsync(x -> {
            Registry.setupRestrictions();
            Registry.registerRestrictionsList();
            THItemStages.LOGGER.info(new TranslatableComponent("thitemstages.commands.reloadstages", StagesHandler.getStages()));
        }, p_10642_);
    }
}
