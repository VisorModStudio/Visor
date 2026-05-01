package org.vmstudio.visor.api.common.network.toclient.vrstate;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorPayloadID;
import org.vmstudio.visor.api.common.network.toclient.VisorPayloadToClient;
import java.util.UUID;

public record VROtherGuiStatePayloadToClient(UUID playerUUID, boolean guiOpened) implements VisorPayloadToClient {
    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeBoolean(guiOpened);
    }
    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.OTHER_VR_GUI_STATE;
    }
    
    public static VROtherGuiStatePayloadToClient read(FriendlyByteBuf buffer) {
        return new VROtherGuiStatePayloadToClient(buffer.readUUID(), buffer.readBoolean());
    }
}