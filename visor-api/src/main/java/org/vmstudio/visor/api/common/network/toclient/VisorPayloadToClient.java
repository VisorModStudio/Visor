package org.vmstudio.visor.api.common.network.toclient;

import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.network.VisorPayload;
import org.vmstudio.visor.api.common.network.VisorPayloadID;
import org.vmstudio.visor.api.common.network.toclient.vrstate.RotationYPayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.vrstate.VROtherActivePayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.vrstate.VROtherStatePayloadToClient;
import net.minecraft.network.FriendlyByteBuf;

public interface VisorPayloadToClient extends VisorPayload {


    static VisorPayloadToClient readPacket(FriendlyByteBuf buffer) {
        int index = buffer.readByte();
        if (index < VisorPayloadID.values().length) {
            VisorPayloadID id = VisorPayloadID.values()[index];
            return switch (id) {
                case HANDSHAKE -> HandshakePayloadToClient.read(buffer);
                case SETTINGS -> SettingsPayloadToClient.read(buffer);
                case ROTATION_Y -> RotationYPayloadToClient.read(buffer);
                case OTHER_VR_ACTIVE -> VROtherActivePayloadToClient.read(buffer);
                case OTHER_VR_STATE -> VROtherStatePayloadToClient.read(buffer);
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
