package org.vmstudio.visor.loader.neoforge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;


public final class ClientTunnelSupport {

    private ClientTunnelSupport() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }


    public static boolean serverAcceptsTunnel() {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection != null
                && connection.hasChannel(VisorRawPayload.TYPE.id());
    }
}
