package org.vmstudio.visor.api.common.network.toclient.vrstate;

import com.google.common.base.Charsets;
import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorPayload;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;

import java.util.UUID;

public record VROtherBodyTypePayloadToClient(UUID playerUUID,
                                             String bodyType) implements VisorPayloadToClient {
    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        buffer.writeBytes(
                bodyType.getBytes(Charsets.UTF_8)
        );
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OTHER_VR_BODY_TYPE.byteOrdinal();
    }



    public static VROtherBodyTypePayloadToClient read(FriendlyByteBuf buffer) {

        return new VROtherBodyTypePayloadToClient(
                buffer.readUUID(),
                VisorPayload.readString(buffer)
        );
    }
}
