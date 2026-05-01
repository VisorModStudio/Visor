package org.vmstudio.visor.api.common.network.toserver.vrstate;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorPayloadID;
import org.vmstudio.visor.api.common.network.toserver.VisorPayloadToServer;

public record GuiStatePayloadToServer(boolean guiOpened) implements VisorPayloadToServer {
    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBoolean(guiOpened);
    }
    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.GUI_STATE;
    }
    
    public static GuiStatePayloadToServer read(FriendlyByteBuf buffer) {
        return new GuiStatePayloadToServer(buffer.readBoolean());
    }
}