package com.decursioteam.thitemstages.network;

import com.decursioteam.thitemstages.datagen.StagesData;
import com.decursioteam.thitemstages.datagen.utils.IStagesData;
import com.decursioteam.thitemstages.events.SyncStagesEvent;
import com.decursioteam.thitemstages.network.messages.SyncStagesMessage;
import com.decursioteam.thitemstages.utils.StagesHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ClientPacketHandler {

    public static SyncStagesMessage decodeStageMessage (FriendlyByteBuf buffer) {
        final String[] stageNames = new String[buffer.readInt()];

        for (int i = 0; i < stageNames.length; i++) {

            stageNames[i] = buffer.readUtf(64);
        }

        return new SyncStagesMessage(stageNames);
    }

    public static void processSyncStagesMessage (SyncStagesMessage message, Supplier<NetworkEvent.Context> context) {

        final IStagesData clientData = new StagesData();

        for (final String stageName : message.getStages()) {
            clientData.addStage(stageName);
        }

        StagesHandler.setClientData(clientData);

        MinecraftForge.EVENT_BUS.post(new SyncStagesEvent(clientData));
    }
}