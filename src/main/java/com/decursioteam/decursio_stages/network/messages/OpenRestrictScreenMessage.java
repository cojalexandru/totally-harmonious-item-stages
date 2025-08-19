package com.decursioteam.decursio_stages.network.messages;

import net.minecraft.network.FriendlyByteBuf;

public class OpenRestrictScreenMessage {
    private final int windowId;

    public OpenRestrictScreenMessage() {
        this(0); // Default window ID
    }

    public OpenRestrictScreenMessage(int windowId) {
        this.windowId = windowId;
    }

    public OpenRestrictScreenMessage(FriendlyByteBuf buffer) {
        this.windowId = buffer.readInt();
    }

    public void encode(FriendlyByteBuf buffer) {
        buffer.writeInt(windowId);
    }

    public int getWindowId() {
        return windowId;
    }
}