package org.vmstudio.visor.api.common.network.toclient.vrstate;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorPayloadID;
import org.vmstudio.visor.api.common.network.toclient.VisorPayloadToClient;

import java.util.UUID;

public record VROtherWorldScalePayloadToClient(UUID playerUUID,
                                               float worldScale) implements VisorPayloadToClient {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeFloat(worldScale);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.OTHER_VR_WORLD_SCALE;
    }


    public static VROtherWorldScalePayloadToClient read(FriendlyByteBuf buffer) {
        return new VROtherWorldScalePayloadToClient(
                buffer.readUUID(),
                buffer.readFloat()
        );
    }
}
