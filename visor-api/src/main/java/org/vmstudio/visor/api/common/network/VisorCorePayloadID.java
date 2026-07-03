package org.vmstudio.visor.api.common.network;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.network.toclient.BlockDamagePayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.HandshakePayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.SettingsPayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.UnknownPayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.vrstate.*;
import org.vmstudio.visor.api.common.network.toserver.*;
import org.vmstudio.visor.api.common.network.toserver.vrstate.*;

public enum VisorCorePayloadID {
    //TO BOTH
    HANDSHAKE,
    ROTATION_Y,
    OFFHAND_SLOT,

    //TO CLIENT
    SERVER_SETTINGS,
    BLOCK_DAMAGE,
    OTHER_VR_POSE_DATA,
    OTHER_VR_LEFT_HANDED,
    OTHER_VR_BODY_TYPE,
    OTHER_VR_WORLD_SCALE,
    OTHER_GUN_ANGLE,
    OTHER_VR_FULL_HEIGHT,

    //TO SERVER
    FULL_HEIGHT,
    POSE_DATA,
    VR_BODY_TYPE,
    LEFT_HANDED,
    ACTIVE_HAND,
    WORLD_SCALE,
    GUN_ANGLE,
    CRAWLING,
    CLIMBING,
    TELEPORT,
    SWING_ATTACK,
    SWING_BLOCK,
    OVERLAY_FOCUSED,
    OTHER_VR_OVERLAY_FOCUSED;


    public byte byteOrdinal() {
        return (byte) this.ordinal();
    }


    public static VisorCorePayloadID fromOrdinal(byte ordinalByte){
        // -127...127 to 0..255
        int unsignedByte = ordinalByte & 0xFF;
        return unsignedByte < values().length ? values()[unsignedByte] : null;
    }

    @Environment(EnvType.CLIENT)
    public static VisorPayloadToClient readToClient(VisorCorePayloadID payloadID,
                                             FriendlyByteBuf buffer) {
        return switch (payloadID) {
            case HANDSHAKE -> HandshakePayloadToClient.read(buffer);
            case OFFHAND_SLOT -> OffhandSlotPayloadToClient.read(buffer);
            case ROTATION_Y -> RotationYPayloadToClient.read(buffer);
            case OTHER_VR_OVERLAY_FOCUSED -> VROtherOverlayFocusedPayloadToClient.read(buffer);
            case OTHER_VR_BODY_TYPE -> VROtherBodyTypePayloadToClient.read(buffer);
            case OTHER_VR_FULL_HEIGHT -> VROtherFullHeightPayloadToClient.read(buffer);
            case OTHER_GUN_ANGLE -> VROtherGunAnglePayloadToClient.read(buffer);
            case OTHER_VR_LEFT_HANDED -> VROtherLeftHandedPayloadToClient.read(buffer);
            case OTHER_VR_POSE_DATA -> VROtherPoseDataPayloadToClient.read(buffer);
            case OTHER_VR_WORLD_SCALE -> VROtherWorldScalePayloadToClient.read(buffer);
            case SERVER_SETTINGS -> SettingsPayloadToClient.read(buffer);
            case BLOCK_DAMAGE -> BlockDamagePayloadToClient.read(buffer);
            default -> {
                VisorAPI.client().getLogger().error(
                        "Visor: Got unexpected payload identifier on client: {}", payloadID
                );
                yield UnknownPayloadToClient.read(buffer);
            }
        };
    }

    public static VisorPayloadToServer readToServer(VisorCorePayloadID payloadID,
                                             FriendlyByteBuf buffer) {
        return switch (payloadID) {
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
            case OVERLAY_FOCUSED -> OverlayFocusedPayloadToServer.read(buffer);
            case TELEPORT -> TeleportMovePayloadToServer.read(buffer);
            default -> {
                VisorAPI.server().getLogger().error(
                        "Visor: Got unexpected payload identifier on server: {}", payloadID
                );
                yield UnknownPayloadToServer.read(buffer);
            }
        };
    }
}
