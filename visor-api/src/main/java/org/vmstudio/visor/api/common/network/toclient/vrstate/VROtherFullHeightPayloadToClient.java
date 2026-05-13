package org.vmstudio.visor.api.common.network.toclient.vrstate;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;

import java.util.UUID;

public record VROtherFullHeightPayloadToClient(UUID playerUUID,
                                               float fullHeight) implements VisorPayloadToClient {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeFloat(fullHeight);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OTHER_VR_FULL_HEIGHT.byteOrdinal();
    }



    public static VROtherFullHeightPayloadToClient read(FriendlyByteBuf buffer) {
        return new VROtherFullHeightPayloadToClient(
                buffer.readUUID(),
                buffer.readFloat()
        );
    }
}
