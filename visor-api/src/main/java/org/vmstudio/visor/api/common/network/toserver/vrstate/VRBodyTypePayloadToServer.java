package org.vmstudio.visor.api.common.network.toserver.vrstate;

import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import net.minecraft.network.FriendlyByteBuf;
import com.google.common.base.Charsets;
import org.vmstudio.visor.api.common.network.VisorPayload;

public record VRBodyTypePayloadToServer(String bodyType) implements VisorPayloadToServer {
    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBytes(
                bodyType.getBytes(Charsets.UTF_8)
        );
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.VR_BODY_TYPE.byteOrdinal();
    }



    public static VRBodyTypePayloadToServer read(FriendlyByteBuf buffer) {

        return new VRBodyTypePayloadToServer(
                VisorPayload.readString(buffer)
        );
    }


}
