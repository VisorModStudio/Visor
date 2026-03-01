package org.vmstudio.visor.api.common.network.toserver;


import org.vmstudio.visor.api.common.network.VisorPayloadID;
import net.minecraft.network.FriendlyByteBuf;

public class ClimbingPayloadToServer implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {

    }

    @Override
    public VisorPayloadID payloadId() {
        return VisorPayloadID.CLIMBING;
    }


    public static ClimbingPayloadToServer read(FriendlyByteBuf buffer) {
        return new ClimbingPayloadToServer();
    }

}
