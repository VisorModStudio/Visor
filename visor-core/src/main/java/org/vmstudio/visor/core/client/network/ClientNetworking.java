package org.vmstudio.visor.core.client.network;

import lombok.Getter;
import org.vmstudio.visor.api.ModLoader;
import org.vmstudio.visor.api.client.player.body.VRBodyType;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.network.buffer.PoseDataBuffer;
import org.vmstudio.visor.api.common.network.toserver.HandshakePayloadToServer;
import org.vmstudio.visor.api.common.network.toserver.VisorPayloadToServer;
import org.vmstudio.visor.api.common.network.toserver.vrstate.*;
import org.vmstudio.visor.api.server.VRServerSettings;
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
import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class ClientNetworking {
    @Getter
    private static boolean serverSupportsVisor = false;

    private static float heightLastSent = 0.0F;
    private static float worldScaleLastSent = 1.0F;
    private static float rotationYLastSent = 0;

    private static boolean leftHandedLastSent = false;
    private static HandType activeHandLastSent = HandType.MAIN;
    private static VRBodyType vrBodyLastSent = null;



    public static void sendVRPacket(VisorPayloadToServer payload) {
        if (MC.getConnection() == null) return;
        if (!serverSupportsVisor) return;
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
                .createPacketToServer(payload);
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

        float worldScale = localPlayer.getPose(PlayerPoseType.TICK).getWorldScale();
        if (worldScale != worldScaleLastSent) {
            sendVRPacket(
                    new WorldScalePayloadToServer(worldScale)
            );
            worldScaleLastSent = worldScale;
        }
        float rotationY = localPlayer.getPose(PlayerPoseType.TICK).getRotationY();
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

        VRBodyType vrBody = localPlayer.getBodyType();
        if(vrBody != vrBodyLastSent){
            sendVRPacket(
                    new VRBodyTypePayloadToServer(vrBody.getId())
            );
            vrBodyLastSent = vrBody;
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
    }

    public static void dispose(){
        serverSupportsVisor = false;
        heightLastSent = 0.0F;
        worldScaleLastSent = 1.0F;
        rotationYLastSent = 0;
        VRClientPlayers.dispose();
    }

}
