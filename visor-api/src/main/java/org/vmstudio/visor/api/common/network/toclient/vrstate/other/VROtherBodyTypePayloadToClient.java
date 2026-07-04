package org.vmstudio.visor.api.common.network.toclient.vrstate.other;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;

import java.util.UUID;

public record VROtherBodyTypePayloadToClient(UUID playerUUID,
                                             String bodyType) implements VisorPayloadToClient {
    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        writeSimple(buffer);
    }
    public void writeSimple(FriendlyByteBuf buffer){
        buffer.writeUtf(bodyType);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OTHER_VR_BODY_TYPE.byteOrdinal();
    }



    public static VROtherBodyTypePayloadToClient read(FriendlyByteBuf buffer) {
        var uuid = buffer.readUUID();
        return readSimple(uuid, buffer);
    }
    public static VROtherBodyTypePayloadToClient readSimple(UUID uuid, FriendlyByteBuf buffer) {
        return new VROtherBodyTypePayloadToClient(
                uuid,
                buffer.readUtf()
        );
    }
}
