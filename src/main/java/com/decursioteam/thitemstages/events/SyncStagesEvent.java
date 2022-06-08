package com.decursioteam.thitemstages.events;

import com.decursioteam.thitemstages.datagen.utils.IStagesData;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerEvent;

@OnlyIn(Dist.CLIENT)
public class SyncStagesEvent extends PlayerEvent {

    private final IStagesData data;

    public SyncStagesEvent(IStagesData data) {

        this(data, Minecraft.getInstance().player);
    }

    public SyncStagesEvent(IStagesData data, PlayerEntity player) {

        super(player);
        this.data = data;
    }

    public IStagesData getData () {

        return this.data;
    }
}
