package com.decursioteam.thitemstages.network;

import com.decursioteam.thitemstages.network.messages.SyncStagesMessage;
import net.minecraft.network.PacketBuffer;

public class ServerPacketHandler {

    public static void encodeStageMessage (SyncStagesMessage packet, PacketBuffer buffer) {

        buffer.writeInt(packet.getStages().length);

        for (final String stageName : packet.getStages()) {

            buffer.writeUtf(stageName, 64);
        }
    }
}