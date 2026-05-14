package org.vmstudio.visor.api.common.network.toclient.vrstate;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;

import java.util.UUID;

public record VROtherWorldScalePayloadToClient(UUID playerUUID,
                                               float worldScale) implements VisorPayloadToClient {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeFloat(worldScale);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OTHER_VR_WORLD_SCALE.byteOrdinal();
    }



    public static VROtherWorldScalePayloadToClient read(FriendlyByteBuf buffer) {
        return new VROtherWorldScalePayloadToClient(
                buffer.readUUID(),
                buffer.readFloat()
        );
    }
}
