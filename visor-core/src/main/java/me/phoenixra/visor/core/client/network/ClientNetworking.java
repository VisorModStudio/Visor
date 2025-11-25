package me.phoenixra.visor.core.client.network;

import lombok.Getter;
import me.phoenixra.visor.api.ModLoader;
import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
import me.phoenixra.visor.api.common.network.buffer.PoseDataBuffer;
import me.phoenixra.visor.api.common.network.toserver.HandshakePayloadToServer;
import me.phoenixra.visor.api.common.network.toserver.VisorPayloadToServer;
import me.phoenixra.visor.api.common.network.toserver.vrstate.*;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.player.VRClientPlayers;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import me.phoenixra.visor.core.client.ClientContext;
import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class ClientNetworking {
    @Getter
    private static boolean serverSupportsVisor = false;

    private static float heightLastSent = 0.0F;
    private static float worldScaleLastSent = 1.0F;
    private static float rotationYLastSent = 0;



    public static void sendVRPacket(VisorPayloadToServer payload) {
        if (MC.getConnection() == null) return;
        if (!serverSupportsVisor) return;
        MC.getConnection().send(createVRPacket(payload));
    }

    public static void sendHandShake(HandshakePayloadToServer payload) {
        if (MC.getConnection() == null) return;
        MC.getConnection().send(createVRPacket(payload));
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

    public static void sendVRPlayerPose() {
        ClientPacketListener connection = MC.getConnection();
        if (connection == null) {
            return;
        }

        var localPlayer = ClientContext.localPlayer;

        float height = localPlayer.getHeight();
        if (height != heightLastSent) {
            sendVRPacket(
                    new HeightPayloadToServer(
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

        PoseDataBuffer vrPlayerState = PoseDataBuffer.create(
                localPlayer,
                localPlayer.isLeftHanded()
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
        if (VisorState.getState().isActive()
                && ClientContext.localPlayer.getHeight() == -1.0F) {
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
        VRClientPlayers.clear();
    }

}
