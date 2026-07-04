package org.vmstudio.visor.api.common.network.toclient.vrstate.other;

import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record VROtherPoseDataPayloadToClient(UUID playerUUID,
                                             PoseDataBuffer pose) implements VisorPayloadToClient {


    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        writeSimple(buffer);
    }
    public void writeSimple(FriendlyByteBuf buffer){
        pose.serialize(buffer);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OTHER_VR_POSE_DATA.byteOrdinal();
    }



    public static VROtherPoseDataPayloadToClient read(FriendlyByteBuf buffer) {
        var uuid = buffer.readUUID();
        return readSimple(uuid, buffer);
    }
    public static VROtherPoseDataPayloadToClient readSimple(UUID uuid, FriendlyByteBuf buffer) {
        return new VROtherPoseDataPayloadToClient(
                uuid,
                PoseDataBuffer.deserialize(buffer)
        );
    }
}
