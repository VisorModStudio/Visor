package org.vmstudio.visor.api.common.network.toserver;


import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import net.minecraft.network.FriendlyByteBuf;

public class ClimbingPayloadToServer implements VisorPayloadToServer {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {

    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.CLIMBING.byteOrdinal();
    }



    public static ClimbingPayloadToServer read(FriendlyByteBuf buffer) {
        return new ClimbingPayloadToServer();
    }


}
