package org.vmstudio.visor.api.common.network.toclient.vrstate;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorPayloadID;
import org.vmstudio.visor.api.common.network.toclient.VisorPayloadToClient;

import java.util.UUID;

public record VROtherLeftHandedPayloadToClient(UUID playerUUID,
                                               boolean leftHanded) implements VisorPayloadToClient {
    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeBoolean(leftHanded);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.OTHER_VR_LEFT_HANDED;
    }


    public static VROtherLeftHandedPayloadToClient read(FriendlyByteBuf buffer) {
        return new VROtherLeftHandedPayloadToClient(
                buffer.readUUID(),
                buffer.readBoolean()
        );
    }
}
