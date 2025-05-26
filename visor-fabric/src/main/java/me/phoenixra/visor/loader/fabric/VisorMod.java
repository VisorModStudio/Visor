package me.phoenixra.visor.loader.fabric;


import me.phoenixra.visor.api.ModLoader;
import me.phoenixra.visor.api.common.network.VisorNetwork;
import me.phoenixra.visor.api.common.network.toclient.VisorPayloadToClient;
import me.phoenixra.visor.api.common.network.toserver.VisorPayloadToServer;
import me.phoenixra.visor.core.common.network.client.ClientPacketHandler;
import me.phoenixra.visor.core.common.network.server.ServerPacketHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

public class VisorMod implements ModInitializer {
    @Override
    public void onInitialize() {


        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            ClientPlayNetworking.registerGlobalReceiver(VisorNetwork.CHANNEL,
                    (client, handler, buffer, responseSender) -> {
                        VisorPayloadToClient packet = VisorPayloadToClient.readPacket(buffer);
                        client.execute(() -> ClientPacketHandler.handlePacket(packet));
                    });


        }

        ServerPlayNetworking.registerGlobalReceiver(VisorNetwork.CHANNEL,
                (server, player, handler, buffer, responseSender) -> {
                    VisorPayloadToServer packet = VisorPayloadToServer.readPacket(buffer);
                    server.execute(() -> ServerPacketHandler.handlePacket(packet, player,
                            p -> responseSender.sendPacket(ModLoader.get().createPacketToClient(p))));
                });


    }
}
