package org.vmstudio.visor.api.common.network.toserver.vrstate;

import com.google.common.base.Charsets;
import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorPayload;
import org.vmstudio.visor.api.common.network.VisorPayloadID;
import org.vmstudio.visor.api.common.network.toclient.SettingsPayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.toserver.VisorPayloadToServer;

public record VRBodyTypePayloadToServer(String bodyType) implements VisorPayloadToServer {
    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeBytes(
                bodyType.getBytes(Charsets.UTF_8)
        );
    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.VR_BODY_TYPE;
    }


    public static VRBodyTypePayloadToServer read(FriendlyByteBuf buffer) {

        return new VRBodyTypePayloadToServer(
                VisorPayload.readString(buffer)
        );
    }
}
