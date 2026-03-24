package org.vmstudio.visor.api.common.network.toclient.vrstate;

import org.vmstudio.visor.api.common.network.VisorPayloadID;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;
import org.vmstudio.visor.api.common.network.toclient.VisorPayloadToClient;
import net.minecraft.network.FriendlyByteBuf;

import java.util.UUID;

public record VROtherPoseDataPayloadToClient(UUID playerUUID,
                                             PoseDataBuffer pose,
                                             float worldScale,
                                             float fullHeight) implements VisorPayloadToClient {


    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        pose.serialize(buffer);
        buffer.writeFloat(worldScale);
        buffer.writeFloat(fullHeight);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.OTHER_VR_POSE_DATA;
    }


    public static VROtherPoseDataPayloadToClient read(FriendlyByteBuf buffer) {
        UUID playerUUID = buffer.readUUID();
        var pose = PoseDataBuffer.deserialize(buffer);
        float worldScale = buffer.readFloat();
        float fullHeight = buffer.readFloat();
        return new VROtherPoseDataPayloadToClient(
                playerUUID,
                pose,
                worldScale,
                fullHeight
        );
    }
}
