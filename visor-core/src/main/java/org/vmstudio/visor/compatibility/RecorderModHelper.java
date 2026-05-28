package org.vmstudio.visor.compatibility;

import net.minecraft.network.protocol.Packet;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.common.network.VisorChannel;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import org.vmstudio.visor.api.common.network.toclient.vrstate.*;
import org.vmstudio.visor.api.common.network.toserver.vrstate.*;
import org.vmstudio.visor.compatibility.flashback.FlashbackCompatHelper;
import org.vmstudio.visor.compatibility.replaymod.ReplayCompatHelper;
import org.vmstudio.visor.core.client.player.VRRemotePlayerImpl;

import java.util.Collection;
import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class RecorderModHelper {

    public static boolean isLoaded() {
        return ReplayCompatHelper.isLoaded() || FlashbackCompatHelper.isLoaded();
    }

    public static boolean isRecording(){
        return ReplayCompatHelper.isRecording() || FlashbackCompatHelper.isRecording();
    }
    public static void sendInitPacketsLocal(VisorChannel channel, List<VisorPayloadToServer> packets){
        for(var packet : packets){
            storeVisorPacketLocal(channel, packet);
        }
    }
    public static void sendInitPacketsRemote(VisorChannel channel,
                                             Collection<VRRemotePlayerImpl> remotePlayers){
        for(var remotePlayer : remotePlayers){
            var uuid = remotePlayer.getMcPlayer().getUUID();
            storePacket(
                    channel,
                    new VROtherPoseDataPayloadToClient(
                            uuid,
                            remotePlayer.getPoseBufferReceived()
                    )
            );
            storePacket(
                    channel,
                    new VROtherBodyTypePayloadToClient(
                            uuid,
                            remotePlayer.getBodyType().getId()
                    )
            );
            storePacket(
                    channel,
                    new VROtherLeftHandedPayloadToClient(
                            uuid,
                            remotePlayer.isLeftHanded()
                    )
            );
            storePacket(
                    channel,
                    new VROtherWorldScalePayloadToClient(
                            uuid,
                            remotePlayer.getPoseData().getWorldScale()
                    )
            );
            storePacket(
                    channel,
                    new VROtherFullHeightPayloadToClient(
                            uuid,
                            remotePlayer.getFullHeight()
                    )
            );
            storePacket(
                    channel,
                    new VROtherGunAnglePayloadToClient(
                            uuid,
                            remotePlayer.getGunAngle()
                    )
            );
            storePacket(
                    channel,
                    new VROtherOverlayFocusedPayloadToClient(
                            uuid,
                            remotePlayer.isOverlayFocused()
                    )
            );
        }
    }

    public static void storeVisorPacketLocal(VisorChannel channel, VisorPayloadToServer payload){
        var selfUUID = MC.player.getUUID();
        VisorPayloadToClient storePayload = null;
        switch (VisorCorePayloadID.fromOrdinal(payload.payloadId())){
            case POSE_DATA -> {
                var payloadInstance = (PoseDataPayloadToServer)payload;
                storePayload = new VROtherPoseDataPayloadToClient(
                        selfUUID,
                        payloadInstance.pose()
                );
            }
            case VR_BODY_TYPE -> {
                var payloadInstance = (VRBodyTypePayloadToServer)payload;
                storePayload = new VROtherBodyTypePayloadToClient(
                        selfUUID,
                        payloadInstance.bodyType()
                );
            }
            case LEFT_HANDED -> {
                var payloadInstance = (LeftHandedPayloadToServer)payload;
                storePayload = new VROtherLeftHandedPayloadToClient(
                        selfUUID,
                        payloadInstance.leftHanded()
                );
            }
            case WORLD_SCALE -> {
                var payloadInstance = (WorldScalePayloadToServer)payload;
                storePayload = new VROtherWorldScalePayloadToClient(
                        selfUUID,
                        payloadInstance.worldScale()
                );
            }
            case FULL_HEIGHT -> {
                var payloadInstance = (FullHeightPayloadToServer)payload;
                storePayload = new VROtherFullHeightPayloadToClient(
                        selfUUID,
                        payloadInstance.fullHeight()
                );
            }
            case GUN_ANGLE -> {
                var payloadInstance = (GunAnglePayloadToServer)payload;
                storePayload = new VROtherGunAnglePayloadToClient(
                        selfUUID,
                        payloadInstance.gunAngle()
                );
            }
            case OVERLAY_FOCUSED -> {
                var payloadInstance = (OverlayFocusedPayloadToServer)payload;
                storePayload = new VROtherOverlayFocusedPayloadToClient(
                        selfUUID,
                        payloadInstance.overlayFocused()
                );
            }
        }
        if(storePayload != null) {
            storePacket(
                    channel,
                    storePayload
            );
        }
    }

    public static void storePacket(VisorChannel channel,
                                    VisorPayloadToClient payload){
        storePacket(
                ModLoader.get().createPacketToClient(
                        channel.getChannelId(), payload
                )
        );
    }
    private static void storePacket(Packet<?> packet) {
        if (FlashbackCompatHelper.isLoaded()) {
            FlashbackCompatHelper.storePacket(packet);
        }
        if (ReplayCompatHelper.isLoaded()) {
            ReplayCompatHelper.storePacket(packet);
        }
    }
}
