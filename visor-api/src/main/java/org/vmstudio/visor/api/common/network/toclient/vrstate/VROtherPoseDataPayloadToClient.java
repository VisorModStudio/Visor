package org.vmstudio.visor.api.common.network.toclient.vrstate;

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
        pose.serialize(buffer);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OTHER_VR_POSE_DATA.byteOrdinal();
    }



    public static VROtherPoseDataPayloadToClient read(FriendlyByteBuf buffer) {
        UUID playerUUID = buffer.readUUID();
        var pose = PoseDataBuffer.deserialize(buffer);
        return new VROtherPoseDataPayloadToClient(
                playerUUID,
                pose
        );
    }
}
