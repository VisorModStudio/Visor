package me.phoenixra.visor.api.common.network.toclient;

import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.common.network.VisorPayload;
import me.phoenixra.visor.api.common.network.VisorPayloadID;
import net.minecraft.network.FriendlyByteBuf;

public interface VisorPayloadToClient extends VisorPayload {


    static VisorPayloadToClient readPacket(FriendlyByteBuf buffer) {
        int index = buffer.readByte();
        if (index < VisorPayloadID.values().length) {
            VisorPayloadID id = VisorPayloadID.values()[index];
            return switch (id) {
                case HANDSHAKE -> HandshakePayloadToClient.read(buffer);
                default -> {
                    VisorAPI.client().getLogger().error(
                            "Visor: Got unexpected payload identifier on client: {}", id
                    );
                    yield UnknownPayloadToClient.read(buffer);
                }
            };
        } else {
            VisorAPI.client().getLogger().error(
                    "Visor: Got unknown payload identifier on client: {}", index
            );
            return UnknownPayloadToClient.read(buffer);
        }
    }
}
