package org.vmstudio.visor.core.client.network;

import net.minecraft.world.entity.player.Player;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.toclient.BlockDamagePayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.SettingsPayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.UnknownPayloadToClient;
import org.vmstudio.visor.api.common.network.VisorPayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.vrstate.*;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.player.VRRemotePlayerImpl;
import org.vmstudio.visor.core.client.tasks.types.TaskHotBar;
import org.vmstudio.visor.core.common.ServerConfig;
import net.minecraft.client.Minecraft;
import org.vmstudio.visor.extensions.client.render.LevelRendererExtension;

public class ClientPacketHandler {


    public static void handlePacket(VisorPayloadToClient payloadClient){
        if (payloadClient instanceof UnknownPayloadToClient) return;

        Minecraft mc = Minecraft.getInstance();
        switch (VisorCorePayloadID.fromOrdinal(payloadClient.payloadId())) {
            case HANDSHAKE -> {
                ClientNetworking.receivedHandShake();
            }
            case SERVER_SETTINGS -> {
                var payload = (SettingsPayloadToClient) payloadClient;

                ServerConfig.updateSettings(
                        VisorAPI.client().getConfigManager(),
                        payload.config()
                );
            }
            case ROTATION_Y -> {
                var payload = (RotationYPayloadToClient) payloadClient;
                ClientContext.localPlayer.setRotationY(
                        payload.rotationY()
                );
            }
            case OFFHAND_SLOT -> {
                var payload = (OffhandSlotPayloadToClient) payloadClient;
                TaskHotBar.getInstance().setOffhandSlot(payload.slot());
            }
            case BLOCK_DAMAGE -> {
                var payload = (BlockDamagePayloadToClient) payloadClient;

                Player player = mc.level.getPlayerByUUID(payload.playerUUID());
                if(player == null){
                    return;
                }
                ((LevelRendererExtension)Minecraft.getInstance().levelRenderer)
                        .visor$damageBlockProgress(
                                player,
                                payload.blockPos(),
                                payload.destroyStage()
                        );
            }
            case OTHER_VR_OVERLAY_FOCUSED -> {
                var payload = (VROtherOverlayFocusedPayloadToClient) payloadClient;
                var remotePlayer = VRClientPlayers.getValidPacketReceiverMc(payload.playerUUID());
                if(remotePlayer == null){
                    return;
                }
                var vrPlayer = VRClientPlayers.ensurePacketReceiver(payload.playerUUID(), remotePlayer);
                vrPlayer.receivedGuiStatePacket(payload.overlayFocused());
            }
            case OTHER_VR_POSE_DATA -> {
                var payload = (VROtherPoseDataPayloadToClient) payloadClient;
                var remotePlayer = VRClientPlayers.getValidPacketReceiverMc(payload.playerUUID());
                if(remotePlayer == null){
                    return;
                }
                var vrPlayer = VRClientPlayers.getPacketReceiver(payload.playerUUID());

                if(vrPlayer == null){
                    vrPlayer = new VRRemotePlayerImpl(
                            remotePlayer,
                            payload.pose()
                    );
                    VRClientPlayers.receivedNewPlayer(vrPlayer);
                }else{
                    vrPlayer.receivedPosePacked(
                            remotePlayer,
                            payload.pose()
                    );
                }
            }
            case OTHER_VR_LEFT_HANDED -> {
                var payload = (VROtherLeftHandedPayloadToClient) payloadClient;
                var remotePlayer = VRClientPlayers.getValidPacketReceiverMc(payload.playerUUID());
                if(remotePlayer == null){
                    return;
                }
                var vrPlayer = VRClientPlayers.ensurePacketReceiver(payload.playerUUID(), remotePlayer);
                vrPlayer.receivedLeftHandedPacket(payload.leftHanded());

            }
            case OTHER_VR_BODY_TYPE -> {
                var payload = (VROtherBodyTypePayloadToClient) payloadClient;
                var remotePlayer = VRClientPlayers.getValidPacketReceiverMc(payload.playerUUID());
                if(remotePlayer == null){
                    return;
                }
                var vrPlayer = VRClientPlayers.ensurePacketReceiver(payload.playerUUID(), remotePlayer);
                vrPlayer.receivedBodyTypePacket(payload.bodyType());
            }
            case OTHER_VR_WORLD_SCALE -> {
                var payload = (VROtherWorldScalePayloadToClient) payloadClient;
                var remotePlayer = VRClientPlayers.getValidPacketReceiverMc(payload.playerUUID());
                if(remotePlayer == null){
                    return;
                }
                var vrPlayer = VRClientPlayers.ensurePacketReceiver(payload.playerUUID(), remotePlayer);
                vrPlayer.receivedWorldScalePacket(payload.worldScale());
            }
            case OTHER_VR_FULL_HEIGHT -> {
                var payload = (VROtherFullHeightPayloadToClient) payloadClient;
                var remotePlayer = VRClientPlayers.getValidPacketReceiverMc(payload.playerUUID());
                if(remotePlayer == null){
                    return;
                }
                var vrPlayer = VRClientPlayers.ensurePacketReceiver(payload.playerUUID(), remotePlayer);
                vrPlayer.receivedFullHeightPacket(payload.fullHeight());
            }
            case OTHER_GUN_ANGLE -> {
                var payload = (VROtherGunAnglePayloadToClient) payloadClient;
                var remotePlayer = VRClientPlayers.getValidPacketReceiverMc(payload.playerUUID());
                if(remotePlayer == null){
                    return;
                }
                var vrPlayer = VRClientPlayers.ensurePacketReceiver(payload.playerUUID(), remotePlayer);
                vrPlayer.receivedGunAngle(payload.gunAngle());
            }

        }
    }
}