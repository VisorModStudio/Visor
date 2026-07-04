package org.vmstudio.visor.api.common.network.toclient.vrstate.other;

import net.minecraft.network.FriendlyByteBuf;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;

import java.util.UUID;

public record VROtherStartTrackingPayloadToClient(UUID playerUUID,
                                                  VROtherPoseDataPayloadToClient poseBuf,
                                                  VROtherBodyTypePayloadToClient bodyTypeBuf,
                                                  VROtherLeftHandedPayloadToClient leftHandedBuf,
                                                  VROtherRotationYPayloadToClient rotationYBuf,
                                                  VROtherWorldScalePayloadToClient worldScaleBuf,
                                                  VROtherFullHeightPayloadToClient fullHeightBuf,
                                                  VROtherGunAnglePayloadToClient gunAngleBuf,
                                                  VROtherOverlayFocusedPayloadToClient overlayFocusedBuf) implements VisorPayloadToClient {

    @Override
    public void onWrite(FriendlyByteBuf buffer) {
        buffer.writeUUID(playerUUID);
        poseBuf.writeSimple(buffer);
        bodyTypeBuf.writeSimple(buffer);
        leftHandedBuf.writeSimple(buffer);
        rotationYBuf.writeSimple(buffer);
        worldScaleBuf.writeSimple(buffer);
        fullHeightBuf.writeSimple(buffer);
        gunAngleBuf.writeSimple(buffer);
        overlayFocusedBuf.writeSimple(buffer);
    }

    @Override
    public byte payloadId() {
        return VisorCorePayloadID.OTHER_VR_START_TRACKING.byteOrdinal();
    }



    public static VROtherStartTrackingPayloadToClient read(FriendlyByteBuf buffer) {
        var uuid = buffer.readUUID();
        return new VROtherStartTrackingPayloadToClient(
                uuid,
                VROtherPoseDataPayloadToClient.readSimple(uuid, buffer),
                VROtherBodyTypePayloadToClient.readSimple(uuid, buffer),
                VROtherLeftHandedPayloadToClient.readSimple(uuid, buffer),
                VROtherRotationYPayloadToClient.readSimple(uuid, buffer),
                VROtherWorldScalePayloadToClient.readSimple(uuid, buffer),
                VROtherFullHeightPayloadToClient.readSimple(uuid, buffer),
                VROtherGunAnglePayloadToClient.readSimple(uuid, buffer),
                VROtherOverlayFocusedPayloadToClient.readSimple(uuid, buffer)
        );
    }
}
