package org.vmstudio.visor.api.common.network.toclient.vrstate.other;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;

import java.util.UUID;

public record VROtherRotationYPayloadToClient (UUID playerUUID,
                                               float rotationY) implements VisorPayloadToClient {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        writeSimple(buffer);
    }
    public void writeSimple(FriendlyByteBuf buffer){
        buffer.writeFloat(rotationY);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OTHER_VR_ROTATION_Y.byteOrdinal();
    }



    public static VROtherRotationYPayloadToClient read(FriendlyByteBuf buffer) {
        var uuid = buffer.readUUID();
        return readSimple(uuid, buffer);
    }
    public static VROtherRotationYPayloadToClient readSimple(UUID uuid, FriendlyByteBuf buffer) {
        return new VROtherRotationYPayloadToClient(
                uuid,
                buffer.readFloat()
        );
    }
}
