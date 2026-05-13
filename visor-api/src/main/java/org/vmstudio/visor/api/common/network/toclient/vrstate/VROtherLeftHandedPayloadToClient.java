package org.vmstudio.visor.api.common.network.toclient.vrstate;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;

import java.util.UUID;

public record VROtherLeftHandedPayloadToClient(UUID playerUUID,
                                               boolean leftHanded) implements VisorPayloadToClient {
    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeBoolean(leftHanded);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OTHER_VR_LEFT_HANDED.byteOrdinal();
    }


    public static VROtherLeftHandedPayloadToClient read(FriendlyByteBuf buffer) {
        return new VROtherLeftHandedPayloadToClient(
                buffer.readUUID(),
                buffer.readBoolean()
        );
    }
}
