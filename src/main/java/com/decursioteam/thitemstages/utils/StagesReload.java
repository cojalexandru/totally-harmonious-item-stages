package com.decursioteam.thitemstages.utils;

import com.decursioteam.thitemstages.Registry;
import com.decursioteam.thitemstages.THItemStages;
import com.decursioteam.thitemstages.datagen.RestrictionsData;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class StagesReload implements IFutureReloadListener {

    @Override
    public CompletableFuture<Void> reload(IStage p_215226_1_, IResourceManager p_215226_2_, IProfiler p_215226_3_, IProfiler p_215226_4_, Executor p_215226_5_, Executor p_215226_6_) {
        return CompletableFuture.completedFuture(null).thenCompose(p_215226_1_::wait).thenAcceptAsync(x -> {
            Registry.setupRestrictions();
            Registry.registerRestrictionsList();
            THItemStages.LOGGER.info(new TranslationTextComponent("thitemstages.commands.reloadstages", StagesHandler.getStages()));
        }, p_215226_5_);
    }
}
