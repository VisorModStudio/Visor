package org.vmstudio.visor.api.common.network.toclient.vrstate;

import org.vmstudio.visor.api.common.network.VisorPayloadID;
import org.vmstudio.visor.api.common.network.toclient.VisorPayloadToClient;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record VROtherActivePayloadToClient(UUID playerUUID,
                                           boolean vrActive) implements VisorPayloadToClient {


    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeBoolean(vrActive);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.OTHER_VR_ACTIVE;
    }


    public static VROtherActivePayloadToClient read(FriendlyByteBuf buffer) {
        return new VROtherActivePayloadToClient(
                buffer.readUUID(),
                buffer.readBoolean()
        );
    }
}
