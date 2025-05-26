package me.phoenixra.visor.core.common.network.client;

import me.phoenixra.visor.api.ModLoader;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.api.common.network.buffer.PlayerPoseBuffer;
import me.phoenixra.visor.api.common.network.toserver.VisorPayloadToServer;
import me.phoenixra.visor.api.common.network.toserver.vrstate.HeightPayloadToServer;
import me.phoenixra.visor.api.common.network.toserver.vrstate.VRPosePayloadToServer;
import me.phoenixra.visor.api.common.network.toserver.vrstate.WorldScalePayloadToServer;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.common.network.client.players.VRRemotePlayers;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import me.phoenixra.visor.core.client.ClientContext;
import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class ClientNetworking {
    public static boolean SERVER_HAS_VISOR = false;

    private static float heightLastSent = 0.0F;
    private static float worldScaleLastSent = 1.0F;


    public static void sendVRPacket(VisorPayloadToServer payload) {
        if (MC.getConnection() == null) return;
        if (!SERVER_HAS_VISOR) return;
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
                            playerHeight / 1.52F
                    )

            );
            heightLastSent = playerHeight;
        }

        float worldScale = ClientContext.player.getWorldScale();
        if (worldScale != worldScaleLastSent) {
            sendVRPacket(
                    new WorldScalePayloadToServer(
                            worldScale
                    )

            );
            worldScaleLastSent = worldScale;
        }

        PlayerPoseBuffer vrPlayerState = PlayerPoseBuffer.create(
                ClientContext.player,
                VRClientSettings.isLeftHanded()
        );
        sendVRPacket(
                new VRPosePayloadToServer(vrPlayerState)
        );

        VRRemotePlayers.getInstance().applyPlayer(
                MC.player.getUUID(),
                vrPlayerState,
                worldScale,
                playerHeight / 1.52F,
                true
        );
    }



    public static void updateClientPose(LocalPlayer player) {
        ClientContext.player
                .updatePlayerLook(
                        player,
                        PoseType.PRE_TICK
                );
    }

    public static void resetServerSettings() {
        heightLastSent = 0.0F;

        // clear VR player data
        VRRemotePlayers.clear();
    }

}
