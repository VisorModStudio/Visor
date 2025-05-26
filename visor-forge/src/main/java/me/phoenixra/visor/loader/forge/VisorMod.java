package me.phoenixra.visor.loader.forge;

import me.phoenixra.visor.api.IModLoader;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.common.network.VisorNetwork;
import me.phoenixra.visor.api.common.network.toclient.VisorPayloadToClient;
import me.phoenixra.visor.api.common.network.toserver.VisorPayloadToServer;
import me.phoenixra.visor.core.common.network.client.ClientPacketHandler;
import me.phoenixra.visor.core.common.network.server.ServerPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.event.EventNetworkChannel;

@Mod(VisorAPI.MOD_ID)
public class VisorMod {
    public static final EventNetworkChannel NETWORK_CHANNEL =
            NetworkRegistry.ChannelBuilder.named(VisorNetwork.CHANNEL)
                    .clientAcceptedVersions(status -> true)
                    .serverAcceptedVersions(status -> true)
                    .networkProtocolVersion(() -> "0")
                    .eventNetworkChannel();

    public VisorMod(){


        NETWORK_CHANNEL.addListener(event -> {
            if (event.getPayload() != null) {
                if (event.getSource().get().getDirection().getOriginationSide().isClient()) {
                    handleServerPacket(event.getPayload(), event.getSource().get());
                } else {
                    handleClientPacket(event.getPayload(), event.getSource().get());
                }
            }
            event.getSource().get().setPacketHandled(true);
        });
    }

    private static void handleClientPacket(FriendlyByteBuf buffer, NetworkEvent.Context context) {
        VisorPayloadToClient packet = VisorPayloadToClient.readPacket(buffer);
        context.enqueueWork(() -> ClientPacketHandler.handlePacket(packet));
    }

    private static void handleServerPacket(FriendlyByteBuf buffer, NetworkEvent.Context context) {
        VisorPayloadToServer packet = VisorPayloadToServer.readPacket(buffer);
        context.enqueueWork(
                () -> ServerPacketHandler.handlePacket(packet, context.getSender(),
                        p -> context.getNetworkManager().send(
                                IModLoader.get().createPacketToClient(p)
                        )
                )
        );
    }
}
