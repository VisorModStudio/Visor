package org.vmstudio.visor.api.common.network.toserver.vrstate;

import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;

public record PoseDataPayloadToServer(PoseDataBuffer pose) implements VisorPayloadToServer {


    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        pose.serialize(buffer);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.POSE_DATA.byteOrdinal();
    }



    public static PoseDataPayloadToServer read(FriendlyByteBuf buffer) {
        var pose = PoseDataBuffer.deserialize(buffer);
        return new PoseDataPayloadToServer(
                pose
        );
    }

}
