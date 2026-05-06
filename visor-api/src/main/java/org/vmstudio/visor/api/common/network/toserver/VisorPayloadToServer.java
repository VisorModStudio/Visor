package org.vmstudio.visor.api.common.network.toserver;

import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.network.VisorPayload;
import org.vmstudio.visor.api.common.network.VisorPayloadID;
import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.toserver.vrstate.*;

public interface VisorPayloadToServer extends VisorPayload {


    static VisorPayloadToServer readPacket(FriendlyByteBuf buffer) {
        int index = buffer.readByte();
        if (index < VisorPayloadID.values().length) {
            VisorPayloadID id = VisorPayloadID.values()[index];
            return switch (id) {
                case HANDSHAKE -> HandshakePayloadToServer.read(buffer);
                case ACTIVE_HAND -> ActiveHandPayloadToServer.read(buffer);
                case CRAWLING -> CrawlingPayloadToServer.read(buffer);
                case FULL_HEIGHT -> FullHeightPayloadToServer.read(buffer);
                case GUN_ANGLE -> GunAnglePayloadToServer.read(buffer);
                case LEFT_HANDED -> LeftHandedPayloadToServer.read(buffer);
                case OFFHAND_SLOT -> OffhandSlotPayloadToServer.read(buffer);
                case POSE_DATA -> PoseDataPayloadToServer.read(buffer);
                case ROTATION_Y -> RotationYPayloadToServer.read(buffer);
                case VR_BODY_TYPE -> VRBodyTypePayloadToServer.read(buffer);
                case WORLD_SCALE -> WorldScalePayloadToServer.read(buffer);
                case CLIMBING -> ClimbingPayloadToServer.read(buffer);
                case SWING_ATTACK -> SwingAttackPayloadToServer.read(buffer);
                case SWING_BLOCK -> SwingBlockPayloadToServer.read(buffer);
                case TELEPORT -> TeleportMovePayloadToServer.read(buffer);
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
