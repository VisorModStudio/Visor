package org.vmstudio.visor.core.client.network;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.player.body.VRBodyType;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.network.VisorChannel;
import org.vmstudio.visor.api.common.network.VisorCorePayloadID;
import org.vmstudio.visor.api.common.network.VisorNetwork;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;
import org.vmstudio.visor.api.common.network.toserver.HandshakePayloadToServer;
import org.vmstudio.visor.api.common.network.VisorPayloadToServer;
import org.vmstudio.visor.api.common.network.toserver.vrstate.*;
import org.vmstudio.visor.api.common.player.VRPlayer;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.compatibility.RecorderModHelper;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.common.addon.CoreAddonClient;
import org.vmstudio.visor.core.server.network.ServerPacketHandler;

import java.util.List;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class ClientNetworking {
    public static VisorChannel CHANNEL;

    @Getter
    private static boolean serverSupportsVisor = false;

    private static float heightLastSent = VRPlayer.DEFAULT_FULL_HEIGHT;
    private static float worldScaleLastSent = 1.0F;
    private static float rotationYLastSent = 0;
    private static int offhandSlotLastSent = -1;
    private static float gunAngleLastSent = VRPlayer.DEFAULT_GUN_ANGLE;

    private static boolean leftHandedLastSent = false;
    private static HandType activeHandLastSent = HandType.MAIN;
    private static VRBodyType vrBodyLastSent = null;
    private static boolean overlayFocusedLastSent = false;


    private static boolean handshakeReceived = false;
    private static boolean recording;

    public static void createClientChannel(@NotNull CoreAddonClient coreAddon){
        CHANNEL =  VisorChannel.builder(
                coreAddon,
                VisorNetwork.CORE_CHANNEL_ID,
                VisorNetwork.CORE_NETWORK_VERSION
        ).toClient(
                (id, buffer)->{
                    VisorCorePayloadID payloadId = VisorCorePayloadID.fromOrdinal(id);
                    return VisorCorePayloadID.readToClient(payloadId, buffer);
                },
                ClientPacketHandler::handlePacket
        ).toServer(
                (id, buffer)->{
                    VisorCorePayloadID payloadId = VisorCorePayloadID.fromOrdinal(id);
                    return VisorCorePayloadID.readToServer(payloadId, buffer);
                },
                ServerPacketHandler::handlePacket
        ).build();
        VisorNetwork.registerChannel(CHANNEL);
    }


    public static void sendVRPacket(VisorPayloadToServer payload) {
        if (MC.getConnection() == null) return;
        if (!serverSupportsVisor) return;
        if(RecorderModHelper.isLoaded()){
            RecorderModHelper.storeVisorPacketLocal(CHANNEL, payload);
        }
        MC.getConnection().send(createVRPacket(payload));
    }

    public static void sendHandShake(HandshakePayloadToServer payload) {
        if (MC.getConnection() == null) return;
        MC.getConnection().send(createVRPacket(payload));
        if(!Minecraft.getInstance().isLocalServer()) {
            VRServerSettings.joinedDedicatedServer();
        }
    }

    public static Packet<?> createVRPacket(VisorPayloadToServer payload) {
        return ModLoader.get()
                .createPacketToServer(VisorNetwork.CORE_CHANNEL_ID, payload);
    }


    public static void sendLookPacket(Player player, Vec3 view) {
        float pitch = (float) Math.toDegrees(Math.asin(-view.y / view.length()));
        float yaw = (float) Math.toDegrees(Mth.atan2(-view.x, view.z));

        ((LocalPlayer) player).connection.send(
                new ServerboundMovePlayerPacket.Rot(
                        yaw, pitch, player.onGround()
                )
        );
    }

    public static void sendVRPlayerState() {
        ClientPacketListener connection = MC.getConnection();
        if (connection == null) {
            return;
        }

        if(!serverSupportsVisor){
            return;
        }

        if(handshakeReceived){
            if(VRClientSettings.tryCalibrateHeight()){
                handshakeReceived = false;
            }
        }

        var localPlayer = ClientContext.localPlayer;
        float height = localPlayer.getFullHeight();
        if (height != heightLastSent) {
            sendVRPacket(
                    new FullHeightPayloadToServer(
                            height
                    )

            );
            heightLastSent = height;
        }

        float worldScale = localPlayer.getPoseData(PlayerPoseType.TICK).getWorldScale();
        if (worldScale != worldScaleLastSent) {
            sendVRPacket(
                    new WorldScalePayloadToServer(worldScale)
            );
            worldScaleLastSent = worldScale;
        }
        float rotationY = localPlayer.getPoseData(PlayerPoseType.TICK).getRotationY();
        if(rotationY != rotationYLastSent){
            sendVRPacket(
                    new RotationYPayloadToServer(rotationY)
            );
            rotationYLastSent = rotationY;
        }

        boolean leftHanded = localPlayer.isLeftHanded();
        if(leftHanded != leftHandedLastSent){
            sendVRPacket(
                    new LeftHandedPayloadToServer(leftHanded)
            );
            leftHandedLastSent = leftHanded;
        }

        HandType activeHamd = localPlayer.getActiveHand();
        if(activeHamd != activeHandLastSent){
            sendVRPacket(
                    new ActiveHandPayloadToServer(activeHamd == HandType.MAIN)
            );
            activeHandLastSent = activeHamd;
        }

        int offhandSlot = localPlayer.getOffhandSlot();
        if(offhandSlot != offhandSlotLastSent){
            sendVRPacket(
                    new OffhandSlotPayloadToServer(offhandSlot)
            );
            offhandSlotLastSent = offhandSlot;
        }

        float gunAngle = localPlayer.getGunAngle();
        if(gunAngle != gunAngleLastSent){
            sendVRPacket(
                    new GunAnglePayloadToServer(gunAngle)
            );
            gunAngleLastSent = gunAngle;
        }

        VRBodyType vrBody = localPlayer.getBodyType();
        if(vrBody != vrBodyLastSent){
            sendVRPacket(
                    new VRBodyTypePayloadToServer(vrBody.getId())
            );
            vrBodyLastSent = vrBody;
        }

        boolean overlayFocused = localPlayer.isOverlayFocused();
        if(overlayFocused != overlayFocusedLastSent){
            sendVRPacket(
                    new OverlayFocusedPayloadToServer(overlayFocused)
            );
            overlayFocusedLastSent = overlayFocused;
        }

        boolean modRecording = RecorderModHelper.isRecording();
        if(modRecording && !recording){
            recording = true;
            RecorderModHelper.sendInitPacketsLocal(CHANNEL,
                    List.of(
                            new FullHeightPayloadToServer(height),
                            new WorldScalePayloadToServer(worldScale),
                            new LeftHandedPayloadToServer(leftHanded),
                            new GunAnglePayloadToServer(gunAngle),
                            new VRBodyTypePayloadToServer(vrBody.getId()),
                            new OverlayFocusedPayloadToServer(overlayFocused)
                    )
            );
            RecorderModHelper.sendInitPacketsRemote(
                    CHANNEL,
                    VRClientPlayers.getRemotePlayers()
            );
        }else if(!modRecording){
            recording = false;
        }


        PoseDataBuffer vrPlayerState = PoseDataBuffer.create(
                localPlayer
        );
        sendVRPacket(
                new PoseDataPayloadToServer(vrPlayerState)
        );

    }


    protected static void receivedHandShake(){
        if (!Minecraft.getInstance().isLocalServer()) {
            MC.gui.getChat().addMessage(
                    Component.translatable(
                            "visor.messages.server_supports"
                    )
            );
        }
        if (VisorState.get().isActive()
                && ClientContext.localPlayer.getFullHeight() == -1.0F) {
            MC.gui.getChat().addMessage(
                    Component.translatable("visor.messages.calibrate_height")
            );
        }
        serverSupportsVisor = true;
        handshakeReceived = true;
    }

    public static void dispose(){
        serverSupportsVisor = false;
        heightLastSent = VRPlayer.DEFAULT_FULL_HEIGHT;
        gunAngleLastSent = VRPlayer.DEFAULT_GUN_ANGLE;
        worldScaleLastSent = 1.0F;
        offhandSlotLastSent = -1;
        vrBodyLastSent = null;
        activeHandLastSent = HandType.MAIN;
        rotationYLastSent = 0;
        overlayFocusedLastSent = false;
        recording = false;
        handshakeReceived = false;
        VRClientPlayers.dispose();
    }

}
