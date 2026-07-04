package org.vmstudio.visor.api.common.network.toclient.vrstate.other;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;

import java.util.UUID;

public record VROtherLeftHandedPayloadToClient(UUID playerUUID,
                                               boolean leftHanded) implements VisorPayloadToClient {
    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        writeSimple(buffer);
    }
    public void writeSimple(FriendlyByteBuf buffer){
        buffer.writeBoolean(leftHanded);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OTHER_VR_LEFT_HANDED.byteOrdinal();
    }


    public static VROtherLeftHandedPayloadToClient read(FriendlyByteBuf buffer) {
        var uuid = buffer.readUUID();
        return readSimple(uuid, buffer);
    }
    public static VROtherLeftHandedPayloadToClient readSimple(UUID uuid, FriendlyByteBuf buffer) {
        return new VROtherLeftHandedPayloadToClient(
                uuid,
                buffer.readBoolean()
        );
    }
}
