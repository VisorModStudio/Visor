package org.vmstudio.visor.core.client.player;

import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.compatibility.mcversion.McVersionClientUtils;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.compatibility.immportals.ImmPortalsCompatHelper;
import org.vmstudio.visor.compatibility.sable.SableCompatHelper;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.player.pose.LocalPlayerPose;
import org.vmstudio.visor.core.client.render.camera.VRCameraEntitySwap;
import org.vmstudio.visor.core.client.render.camera.VRCameraEntityCache;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VRAimPicker {

    @Getter
    private static Vec3 aimHitPos;
    private static HandType pickingHand;
    private static final HitResult[] handHitResult = new HitResult[2];
    private static final Vec3[] handAimHitPos = new Vec3[2];
    private static final Entity[] handPickEntity = new Entity[2];


    public static void pickWithVRHands(Runnable vanillaPick) {
        if(VisorState.get().isNotActive()){
            vanillaPick.run();
            return;
        }
        if (McVersionClientUtils.screen() != null && MC.hitResult != null) {
            return;
        }
        if (MC.getCameraEntity() == null) {
            MC.hitResult = fallbackMiss();
            return;
        }

        HandType activeHand = ClientContext.localPlayer.getActiveHand();
        pickWithHand(activeHand, vanillaPick);

        HandType otherHand = activeHand.opposite();
        if (VRServerSettings.isTwoHandedVR()
                && ClientContext.rawPoseHandler.getControllerData(otherHand).isTracking()) {
            HitResult activeHit = MC.hitResult;
            Entity activePickEntity = MC.crosshairPickEntity;

            pickWithHand(otherHand, vanillaPick);

            MC.hitResult = activeHit;
            MC.crosshairPickEntity = activePickEntity;
            aimHitPos = handAimHitPos[activeHand.ordinal()];
        } else {
            handHitResult[otherHand.ordinal()] = null;
            handAimHitPos[otherHand.ordinal()] = null;
            handPickEntity[otherHand.ordinal()] = null;
        }
    }

    public static Vec3 pickPos(Vec3 original) {
        if (VisorState.get().isNotActive()) {
            return original;
        }
        LocalPlayerPose renderPose = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.RENDER);

        HandType hand = pickingHand != null
                ? pickingHand
                : ClientContext.localPlayer.getActiveHand();

        HitResult hitResult = pickBlock(
                renderPose.getHand(hand),
                McVersionClientUtils.blockPickRange(MC.gameMode, MC.player),
                false
        );
        MC.hitResult = hitResult;
        Vec3 fallbackAimHitPos = pointAlongAim(
                renderPose.getHand(hand),
                McVersionClientUtils.blockPickRange(MC.gameMode, MC.player)
        );
        aimHitPos = hitResult != null && hitResult.getType() != HitResult.Type.MISS
                ? SableCompatHelper.isLoaded() ? SableCompatHelper.toWorldPos(MC.level, hitResult, hitResult.getLocation()) : hitResult.getLocation()
                : fallbackAimHitPos;

        return new Vec3((Vector3f) renderPose.getHand(hand).getPosition());
    }

    public static Vec3 pickDirection(Vec3 original) {
        if (VisorState.get().isNotActive()) {
            return original;
        }
        HandType hand = pickingHand != null
                ? pickingHand
                : ClientContext.localPlayer.getActiveHand();

        return new Vec3(
                (Vector3f) ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER)
                        .getHand(hand).getDirection()
        );
    }

    // block hit already traced by pickPos; null = let vanilla Entity.pick trace it
    @Nullable
    public static HitResult vrBlockPick() {
        if (VisorState.get().isNotActive()) {
            return null;
        }
        return MC.hitResult;
    }

    @Nullable
    public static Vec3 getAimHitPos(HandType hand) {
        return handAimHitPos[hand.ordinal()];
    }

    @Nullable
    public static HitResult getHandHitResult(HandType hand) {
        return handHitResult[hand.ordinal()];
    }

    /**
     * Applies the last computed pick of the given hand
     * to the game hit result, so hand switching
     * doesn't act with stale aim data
     */
    public static void applyHandPick(HandType hand) {
        HitResult hitResult = handHitResult[hand.ordinal()];
        Vec3 aimHitPos = handAimHitPos[hand.ordinal()];
        if (hitResult == null || aimHitPos == null) {
            //? if >=26.1 {
            MC.pick(1.0f);
            //?} else {
            /*MC.gameRenderer.pick(1.0f);
            *///?}
            return;
        }
        MC.hitResult = hitResult;
        MC.crosshairPickEntity = handPickEntity[hand.ordinal()];
        VRAimPicker.aimHitPos = aimHitPos;
    }

    private static BlockHitResult fallbackMiss() {
        var player = MC.player;
        if (player == null) {
            return BlockHitResult.miss(Vec3.ZERO, Direction.UP, BlockPos.ZERO);
        }
        return BlockHitResult.miss(player.position(), player.getDirection(), player.blockPosition());
    }

    private static void pickWithHand(HandType hand, Runnable vanillaPick) {
        pickingHand = hand;

        Entity cameraEntity = MC.getCameraEntity();
        AABB originalBB = cameraEntity.getBoundingBox();
        VRCameraEntitySwap.cacheCameraEntity(cameraEntity);
        VRCameraEntitySwap.setupCameraEntity(
                ClientContext.localPlayer
                        .getPoseData(PlayerPoseType.RENDER)
                        .getHand(hand)
        );
        VRCameraEntityCache cameraEntityCache = VRCameraEntitySwap.getCameraEntityCache();
        double shiftX = cameraEntity.getX() - cameraEntityCache.getX();
        double shiftY = cameraEntity.getY() - cameraEntityCache.getY();
        double shiftZ = cameraEntity.getZ() - cameraEntityCache.getZ();
        cameraEntity.setBoundingBox(originalBB.move(shiftX, shiftY, shiftZ));

        vanillaPick.run();

        VRCameraEntitySwap.restoreCameraEntity(cameraEntity);
        cameraEntity.setBoundingBox(originalBB);

        HitResult hitResult = MC.hitResult;
        if (hitResult != null && hitResult.getType() != HitResult.Type.MISS) {
            // includes entity hits missed by pickPos
            if (SableCompatHelper.isLoaded()) {
                aimHitPos = SableCompatHelper.toWorldPos(
                        MC.level,
                        hitResult,
                        hitResult.getLocation()
                );
            } else {
                aimHitPos = hitResult.getLocation();
            }
        }
        handHitResult[hand.ordinal()] = hitResult;
        handAimHitPos[hand.ordinal()] = aimHitPos;
        handPickEntity[hand.ordinal()] = MC.crosshairPickEntity;
        pickingHand = null;
    }

    private static Vec3 pointAlongAim(VRPose vrPose,
                                      double distance) {
        var dir = vrPose.getDirection();
        return new Vec3(vrPose
                .getPosition().add(
                        dir.x() * (float) distance,
                        dir.y() * (float) distance,
                        dir.z() * (float) distance,
                        new Vector3f()
                )
        );
    }

    private static HitResult pickBlock(VRPose vrPose,
                                       double blockReachDistance,
                                       boolean fluid
    ) {
        return ImmPortalsCompatHelper.pickBlock(MC.level, vrPose, blockReachDistance, fluid, MC.player);
    }
}
