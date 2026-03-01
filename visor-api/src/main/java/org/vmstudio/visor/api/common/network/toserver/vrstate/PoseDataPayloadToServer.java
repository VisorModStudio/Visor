package org.vmstudio.visor.api.common.network.toserver.vrstate;

import org.vmstudio.visor.api.common.network.VisorPayloadID;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;
import org.vmstudio.visor.api.common.network.toserver.VisorPayloadToServer;
import net.minecraft.network.FriendlyByteBuf;


public record PoseDataPayloadToServer(PoseDataBuffer pose) implements VisorPayloadToServer {


    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        pose.serialize(buffer);
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.POSE_DATA;
    }


    public static PoseDataPayloadToServer read(FriendlyByteBuf buffer) {
        var pose = PoseDataBuffer.deserialize(buffer);
        return new PoseDataPayloadToServer(
                pose
        );
    }
}
