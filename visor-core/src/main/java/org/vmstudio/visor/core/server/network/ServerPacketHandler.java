package org.vmstudio.visor.core.server.network;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.network.*;
import org.vmstudio.visor.api.common.network.toclient.HandshakePayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.SettingsPayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.vrstate.OffhandSlotPayloadToClient;
import org.vmstudio.visor.api.common.network.toclient.vrstate.RotationYPayloadToClient;
import org.vmstudio.visor.api.common.network.toserver.*;
import org.vmstudio.visor.api.common.network.toserver.vrstate.*;
import org.vmstudio.visor.api.server.SupportedMovement;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.core.common.ServerConfig;
import org.vmstudio.visor.core.server.player.VRServerPlayerImpl;
import org.vmstudio.visor.core.server.VisorServerImpl;
import org.vmstudio.visor.core.server.player.VisorServerPlayerImpl;
import org.vmstudio.visor.extensions.common.PlayerExtension;
import org.vmstudio.visor.extensions.common.ServerPlayerExtension;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.Logger;
import org.vmstudio.visor.extensions.common.ServerPlayerGameModeExtension;

import java.util.function.Consumer;

import static net.minecraft.server.network.ServerGamePacketListenerImpl.MAX_INTERACTION_DISTANCE;

public class ServerPacketHandler {


    public static void handlePacket(VisorPayloadToServer payloadToServer,
                                    ServerPlayer serverPlayer,
                                    Consumer<VisorPayloadToClient> packetConsumer){

        VisorServerPlayerImpl packetReceiver = VisorServerImpl.INSTANCE.getVisorPlayer(serverPlayer);

        VRServerPlayerImpl vrPlayer = (VRServerPlayerImpl) VisorServerImpl.INSTANCE.getVRPlayer(serverPlayer);

        if (vrPlayer == null) {
            if(payloadToServer.payloadId() != VisorCorePayloadID.HANDSHAKE.byteOrdinal()) {
                return;
            } else{
                if(packetReceiver == null){
                    var payload = (HandshakePayloadToServer) payloadToServer;
                    handleHandshake(
                            serverPlayer,
                            packetConsumer,
                            payload.vrActive(),
                            payload.networkVersion(),
                            payload.visorVersion()
                    );
                }else{
                    //packets for nonVR
                }
            }
            return;
        }

        VisorServerImpl.INSTANCE.updateMcPlayer(serverPlayer);

        switch (VisorCorePayloadID.fromOrdinal(payloadToServer.payloadId())) {
            case HANDSHAKE -> {

            }
            case POSE_DATA -> {
                var payload = (PoseDataPayloadToServer) payloadToServer;

                vrPlayer.receivedPosePacket(
                        payload.pose()
                );
            }
            case LEFT_HANDED -> {
                var payload = (LeftHandedPayloadToServer) payloadToServer;

                vrPlayer.setLeftHanded(payload.leftHanded());
            }
            case ACTIVE_HAND -> {
                var payload = (ActiveHandPayloadToServer) payloadToServer;

                vrPlayer.setActiveHand(
                        payload.activeHandMain()
                        ? HandType.MAIN : HandType.OFFHAND
                );
            }
            case OFFHAND_SLOT -> {
                var payload = (OffhandSlotPayloadToServer) payloadToServer;

                vrPlayer.updateOffhandSlot(
                        payload.slot()
                );
            }
            case VR_BODY_TYPE -> {
                var payload = (VRBodyTypePayloadToServer) payloadToServer;

                vrPlayer.setVrBodyType(payload.bodyType());
            }
            case WORLD_SCALE -> {
                var payload = (WorldScalePayloadToServer) payloadToServer;
                vrPlayer.setWorldScale(payload.worldScale());
            }
            case FULL_HEIGHT -> {
                var payload = (FullHeightPayloadToServer) payloadToServer;
                vrPlayer.setFullHeight(payload.fullHeight());
            }
            case ROTATION_Y -> {
                var payload = (RotationYPayloadToServer) payloadToServer;
                vrPlayer.updateRotationY(payload.rotationY());
            }
            case GUN_ANGLE -> {
                var payload = (GunAnglePayloadToServer) payloadToServer;
                vrPlayer.setGunAngle(payload.gunAngle());
            }
            case OVERLAY_FOCUSED -> {
                var payload = (OverlayFocusedPayloadToServer) payloadToServer;
                vrPlayer.setOverlayFocused(payload.overlayFocused());
            }
            case CRAWLING -> {
                if(!VRServerSettings.isRoomCrawlingSupported()){
                    return;
                }
                var payload = (CrawlingPayloadToServer) payloadToServer;
                vrPlayer.setCrawling(payload.crawling());
            }
            case CLIMBING -> {
                if(!VRServerSettings.isRoomClimbingSupported()){
                    return;
                }
                vrPlayer.getMcPlayer().fallDistance = 0.0F;
            }
            case TELEPORT -> {
                if(VRServerSettings.getSupportedMovement() == SupportedMovement.CONTROLLER){
                    return;
                }
                var payload = (TeleportMovePayloadToServer) payloadToServer;
                ServerPlayer player = vrPlayer.getMcPlayer();
                player.absMoveTo(
                        payload.x(), payload.y(), payload.z(),
                        player.getYRot(),
                        player.getXRot()
                );
            }
            case SWING_ATTACK -> {
                if(!VRServerSettings.isBetterSwinging()){
                    return;
                }
                if (serverPlayer.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                    return;
                }

                var payload = (SwingAttackPayloadToServer) payloadToServer;

                ServerLevel serverLevel = serverPlayer.serverLevel();
                HandType handType = payload.mainHand() ? HandType.MAIN : HandType.OFFHAND;

                Entity entity = serverLevel.getEntityOrPart(
                        payload.entityId()
                );

                serverPlayer.resetLastActionTime();
                serverPlayer.setShiftKeyDown(payload.shiftKeyDown());
                if (entity != null) {
                    if (!serverLevel.getWorldBorder().isWithinBounds(entity.blockPosition())) {
                        return;
                    }

                    AABB aABB = entity.getBoundingBox();
                    if (aABB.distanceToSqr(serverPlayer.getEyePosition()) < MAX_INTERACTION_DISTANCE) {

                        if (!(entity instanceof ItemEntity) && !(entity instanceof ExperienceOrb)
                                && !(entity instanceof AbstractArrow) && entity != serverPlayer) {
                            ItemStack itemStack = serverPlayer.getItemInHand(
                                    handType.asInteractionHand()
                            );
                            if (itemStack.isItemEnabled(serverLevel.enabledFeatures())) {
                                if (serverPlayer.gameMode.getGameModeForPlayer()
                                        == GameType.SPECTATOR) {
                                    serverPlayer.setCamera(entity);
                                }else {
                                    ((PlayerExtension)serverPlayer)
                                            .visor$swingAttack(entity, handType);
                                }
                            }
                        } else {
                            serverPlayer.connection.disconnect(Component.translatable("multiplayer.disconnect.invalid_entity_attacked"));
                            VisorAPI.server().getLogger().warn("Player {} tried to attack an invalid entity", serverPlayer.getName().getString());
                        }

                    }
                }
            }
            case SWING_BLOCK -> {
                if(!VRServerSettings.isBetterSwinging()){
                    return;
                }
                if (serverPlayer.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
                    return;
                }

                var payload = (SwingBlockPayloadToServer) payloadToServer;

                HandType handType = payload.mainHand() ? HandType.MAIN : HandType.OFFHAND;

                ItemStack itemStack = serverPlayer.getItemInHand(
                        handType.asInteractionHand()
                );
                serverPlayer.resetLastActionTime();
                ((ServerPlayerGameModeExtension) serverPlayer.gameMode)
                        .visor$handleVrBlockDamage(
                                payload.blockPos(),
                                payload.direction(),
                                serverPlayer.level().getMaxBuildHeight(),
                                payload.sequence(),
                                itemStack
                        );
                serverPlayer.connection.ackBlockChangesUpTo(payload.sequence());
            }
        }
    }

    private static void handleHandshake(ServerPlayer player,
                                        Consumer<VisorPayloadToClient> packetConsumer,
                                        boolean vrActive,
                                        int networkVersion,
                                        String visorVersion){
        Logger logger = VisorServerImpl.INSTANCE.getLogger();

        if (VRServerSettings.isServerDebug()) {
            logger.info(
                    "Visor: player '{}' joined with {}",
                    player.getName().getString(),
                    visorVersion
            );
        }

        // check if client supports a supported version
        if (networkVersion == VisorNetwork.CORE_NETWORK_VERSION)
        {
            if (VRServerSettings.isServerDebug()) {
                logger.info("Player {} has supported Visor network version",
                        player.getName().getString(),
                        networkVersion
                );
            }
        } else {
            // unsupported version, send notification, and disregard
            player.connection.disconnect(
                    Component.translatable("visor.messages.network_mismatch", networkVersion, VisorNetwork.CORE_NETWORK_VERSION)
            );
            if (VRServerSettings.isServerDebug()) {
                logger.info(
                        """
                                Player {} has unsupported Visor network version...\
                                
                                Player: {} Server: {}\
                                
                                Disconnecting...""",
                        player.getName(),
                        networkVersion,  VisorNetwork.CORE_NETWORK_VERSION
                );
            }
            return;
        }
        VRServerPlayerImpl vrPlayer;
        VisorServerPlayerImpl packetReceiver;
        if(vrActive){
            vrPlayer = new VRServerPlayerImpl(player);
            if (VRServerSettings.isServerDebug()) {
                VisorServerImpl.LOGGER.info(
                        "VR: player '{}' joined with {}",
                        vrPlayer.getMcPlayer().getName().getString(),
                        visorVersion
                );
            }
            VisorServerImpl.INSTANCE.addVisorPlayer(vrPlayer);

            packetConsumer.accept(
                    new HandshakePayloadToClient()
            );
            packetConsumer.accept(
                    new SettingsPayloadToClient(
                            ServerConfig
                                    .getSettingsForClient()
                                    .toPlaintext()
                    )
            );
            packetConsumer.accept(
                    new RotationYPayloadToClient(
                            ((ServerPlayerExtension)player).visor$getRotationYCached()
                    )
            );
            if(VRServerSettings.isTwoHandedVR()) {
                packetConsumer.accept(
                        new OffhandSlotPayloadToClient(
                                ((ServerPlayerExtension) player).visor$getOffhandSlotCached()
                        )
                );
            }
        }else{
            packetReceiver = new VisorServerPlayerImpl(player);
            if (VRServerSettings.isServerDebug()) {
                VisorServerImpl.LOGGER.info(
                        "NonVR: player '{}' joined with {}",
                        packetReceiver.getMcPlayer().getName().getString(),
                        visorVersion
                );
            }
            VisorServerImpl.INSTANCE.addVisorPlayer(packetReceiver);

            packetConsumer.accept(
                    new HandshakePayloadToClient()
            );
            packetConsumer.accept(
                    new SettingsPayloadToClient(
                            ServerConfig
                                    .getSettingsForClient()
                                    .toPlaintext()
                    )
            );
        }




    }

}
