package org.vmstudio.visor.api.common.network.toserver.vrstate;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToServer;

public record GuiStatePayloadToServer(boolean guiOpened) implements VisorPayloadToServer {
    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBoolean(guiOpened);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.GUI_STATE.byteOrdinal();
    }

    public static GuiStatePayloadToServer read(FriendlyByteBuf buffer) {
        return new GuiStatePayloadToServer(buffer.readBoolean());
    }
}