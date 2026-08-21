package org.vmstudio.visor.loader.neoforge.network;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Client-only classes kept out of {@code NeoForgeModLoader}, which also loads on the
 * dedicated server.
 */
public final class ClientChannelSupport {

    private ClientChannelSupport() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }


    /**
     * NeoForge throws on sending a custom payload the server has not declared, so only send
     * once the channel is known for this connection: negotiated with a NeoForge server, or
     * advertised through {@code minecraft:register} by a Fabric/Forge/Paper one (which happens
     * right after the login packet - see {@code ClientNetworking#sendHandShake}).
     */
    public static boolean serverAccepts(@NotNull ResourceLocation channelId) {
        ClientPacketListener connection = Minecraft.getInstance().getConnection();
        return connection != null
                && connection.hasChannel(channelId);
    }
}
