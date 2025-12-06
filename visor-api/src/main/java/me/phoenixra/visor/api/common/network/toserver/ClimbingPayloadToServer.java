package me.phoenixra.visor.api.common.network.toserver;


import me.phoenixra.visor.api.common.network.VisorPayloadID;
import me.phoenixra.visor.api.common.network.toserver.vrstate.VRActivePayloadToServer;
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
