package org.vmstudio.visor.api.common.network.toclient.vrstate;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;

import java.util.UUID;

public record VROtherGuiStatePayloadToClient(UUID playerUUID, boolean guiOpened) implements VisorPayloadToClient {
    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeBoolean(guiOpened);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OTHER_VR_GUI_STATE.byteOrdinal();
    }

    public static VROtherGuiStatePayloadToClient read(FriendlyByteBuf buffer) {
        return new VROtherGuiStatePayloadToClient(buffer.readUUID(), buffer.readBoolean());
    }
}