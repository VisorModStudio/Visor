package org.vmstudio.visor.api.common.network.toclient.vrstate.other;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;

import java.util.UUID;

public record VROtherWorldScalePayloadToClient(UUID playerUUID,
                                               float worldScale) implements VisorPayloadToClient {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        writeSimple(buffer);
    }
    public void writeSimple(FriendlyByteBuf buffer){
        buffer.writeFloat(worldScale);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OTHER_VR_WORLD_SCALE.byteOrdinal();
    }



    public static VROtherWorldScalePayloadToClient read(FriendlyByteBuf buffer) {
        var uuid = buffer.readUUID();
        return readSimple(uuid, buffer);
    }
    public static VROtherWorldScalePayloadToClient readSimple(UUID uuid, FriendlyByteBuf buffer) {
        return new VROtherWorldScalePayloadToClient(
                uuid,
                buffer.readFloat()
        );
    }
}
