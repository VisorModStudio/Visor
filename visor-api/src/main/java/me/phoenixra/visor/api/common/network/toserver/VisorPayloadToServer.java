package me.phoenixra.visor.api.common.network.toserver;

import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.common.network.VisorPayload;
import me.phoenixra.visor.api.common.network.VisorPayloadID;
import me.phoenixra.visor.api.common.network.toserver.vrstate.*;
import net.minecraft.network.FriendlyByteBuf;

public interface VisorPayloadToServer extends VisorPayload {


    static VisorPayloadToServer readPacket(FriendlyByteBuf buffer) {
        int index = buffer.readByte();
        if (index < VisorPayloadID.values().length) {
            VisorPayloadID id = VisorPayloadID.values()[index];
            return switch (id) {
                case HANDSHAKE -> HandshakePayloadToServer.read(buffer);
                case HEIGHT -> HeightPayloadToServer.read(buffer);
                case ROTATION_Y -> RotationYPayloadToServer.read(buffer);
                case VR_ACTIVE -> VRActivePayloadToServer.read(buffer);
                case POSE_DATA -> PoseDataPayloadToServer.read(buffer);
                case WORLD_SCALE -> WorldScalePayloadToServer.read(buffer);
                default -> {
                    VisorAPI.server().getLogger().error(
                            "Visor: Got unexpected payload identifier on server: {}", id
                    );
                    yield UnknownPayloadToServer.read(buffer);
                }
            };
        } else {
            VisorAPI.server().getLogger().error("Visor: Got unknown payload identifier on server: {}", index);
            return UnknownPayloadToServer.read(buffer);
        }
    }
}
