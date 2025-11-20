package me.phoenixra.visor.core.client.network;

import lombok.Getter;
import lombok.Setter;
import me.phoenixra.visor.api.ModLoader;
import me.phoenixra.visor.api.common.network.buffer.PlayerPoseBuffer;
import me.phoenixra.visor.api.common.network.toserver.HandshakePayloadToServer;
import me.phoenixra.visor.api.common.network.toserver.VisorPayloadToServer;
import me.phoenixra.visor.api.common.network.toserver.vrstate.HeightPayloadToServer;
import me.phoenixra.visor.api.common.network.toserver.vrstate.PoseDataPayloadToServer;
import me.phoenixra.visor.api.common.network.toserver.vrstate.RotationYPayloadToServer;
import me.phoenixra.visor.api.common.network.toserver.vrstate.WorldScalePayloadToServer;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.network.players.VRRemotePlayers;
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

        float playerHeight = VRClientSettings.getPlayerHeight();
        if (playerHeight != heightLastSent) {
            sendVRPacket(
                    new HeightPayloadToServer(
                            playerHeight
                    )

            );
            heightLastSent = playerHeight;
        }

        float worldScale = ClientContext.player.getWorldScale();
        if (worldScale != worldScaleLastSent) {
            sendVRPacket(
                    new WorldScalePayloadToServer(worldScale)
            );
            worldScaleLastSent = worldScale;
        }
        float rotationY = ClientContext.player.getRotationY();
        if(rotationY != rotationYLastSent){
            sendVRPacket(
                    new RotationYPayloadToServer(rotationY)
            );
            rotationYLastSent = rotationY;
        }

        PlayerPoseBuffer vrPlayerState = PlayerPoseBuffer.create(
                ClientContext.player,
                VRClientSettings.isLeftHanded()
        );
        sendVRPacket(
                new PoseDataPayloadToServer(vrPlayerState)
        );

        VRRemotePlayers.getInstance().applyPlayer(
                MC.player.getUUID(),
                vrPlayerState,
                worldScale,
                playerHeight,
                true
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
                && VRClientSettings.getPlayerHeight() == -1.0F) {
            MC.gui.getChat().addMessage(
                    Component.translatable("visor.messages.calibrate_height")
            );
        }
        serverSupportsVisor = true;
    }

    public static void dispose(){
        serverSupportsVisor = false;
        VRRemotePlayers.clear();
    }

}
