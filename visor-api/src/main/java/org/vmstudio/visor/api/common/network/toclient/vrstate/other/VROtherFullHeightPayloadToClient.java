package org.vmstudio.visor.api.common.network.toclient.vrstate.other;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;

import java.util.UUID;

public record VROtherFullHeightPayloadToClient(UUID playerUUID,
                                               float fullHeight) implements VisorPayloadToClient {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        writeSimple(buffer);
    }
    public void writeSimple(FriendlyByteBuf buffer){
        buffer.writeFloat(fullHeight);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OTHER_VR_FULL_HEIGHT.byteOrdinal();
    }



    public static VROtherFullHeightPayloadToClient read(FriendlyByteBuf buffer) {
        var uuid = buffer.readUUID();
        return readSimple(uuid, buffer);
    }
    public static VROtherFullHeightPayloadToClient readSimple(UUID uuid, FriendlyByteBuf buffer) {
        return new VROtherFullHeightPayloadToClient(
                uuid,
                buffer.readFloat()
        );
    }
}
