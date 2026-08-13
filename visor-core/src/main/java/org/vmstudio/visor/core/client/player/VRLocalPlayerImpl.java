package org.vmstudio.visor.core.client.player;

import lombok.Getter;

import lombok.Setter;
import me.phoenixra.atumvr.api.input.haptics.AtumVRBodyHaptics;
import me.phoenixra.atumvr.api.input.treadmill.AtumVRTreadmillView;
import org.jetbrains.annotations.Nullable;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.events.BodyChangedVREvent;
import org.vmstudio.visor.api.client.events.InRoomMoveVREvent;
import org.vmstudio.visor.api.client.player.VRLocalPlayer;
import org.vmstudio.visor.api.client.player.body.VRBodyType;
import org.vmstudio.visor.api.client.player.pose.*;
import org.vmstudio.visor.api.client.tasks.VisorTask;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.network.toserver.vrstate.ActiveHandPayloadToServer;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.player.pose.LocalPlayerPose;
import org.vmstudio.visor.core.client.tasks.types.TaskHotBar;
import org.vmstudio.visor.core.client.tasks.types.movement.TaskRoomClimb;
import org.vmstudio.visor.core.client.tasks.types.movement.TaskRoomCrawl;
import org.vmstudio.visor.core.common.CommonUtils;
import org.vmstudio.visor.core.common.player.PoseHistoryImpl;
import org.vmstudio.visor.extensions.client.entity.LocalPlayerExtension;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.api.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.tasks.types.movement.vehicle.TaskVehicle;
import org.vmstudio.visor.core.client.network.ClientNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import org.vmstudio.visor.core.client.ClientContext;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

public class VRLocalPlayerImpl implements VRLocalPlayer {

    private final LocalPlayerPose roomPose;

    private final LocalPlayerPose prevPose;
    private final LocalPlayerPose pose;
    private final LocalPlayerPose renderPose;

    @Getter
    private final PoseHistoryImpl poseHistoryRoom;
    @Getter
    private final PoseHistoryImpl poseHistoryTick;

    @Getter
    private VRBodyType bodyType;

    @Getter
    private HandType activeHand = HandType.MAIN;


    private float rotationYRaw;

    private boolean isTicking;

    @Getter @Setter
    private boolean bodyChangeable = true;

    @Getter
    private final Vector2f movement = new Vector2f();
    @Getter @Setter
    private boolean moving;
    @Getter @Setter
    private boolean overlayFocused;

    public VRLocalPlayerImpl() {
        this.roomPose = new LocalPlayerPose(this, PlayerPoseType.ROOM);
        this.prevPose = new LocalPlayerPose(this, PlayerPoseType.PREV_TICK);
        this.pose = new LocalPlayerPose(this, PlayerPoseType.TICK);
        this.renderPose  = new LocalPlayerPose(this, PlayerPoseType.RENDER);

        this.poseHistoryRoom = new PoseHistoryImpl(roomPose);
        this.poseHistoryTick = new PoseHistoryImpl(pose);
    }

    @Override
    public void setBodyType(@NotNull VRBodyType bodyType) {
        boolean sendEvent = this.bodyType != null && this.bodyType != bodyType;
        this.bodyType = bodyType;

        this.roomPose.bodyTypeChanged(bodyType);
        this.prevPose.bodyTypeChanged(bodyType);
        this.pose.bodyTypeChanged(bodyType);
        this.renderPose.bodyTypeChanged(bodyType);

        this.poseHistoryRoom.clear();
        this.poseHistoryTick.clear();
        if(sendEvent){
            VisorAPI.eventBus().callEvent(
                    new BodyChangedVREvent(this, bodyType)
            );
        }
    }

    @Override
    public void setActiveHand(@NotNull HandType activeHand) {
        if(this.activeHand == activeHand){
            return;
        }
        this.activeHand = activeHand;

        if (VisorState.get().isActive() && MC.level != null) {
            // refresh aim data, so the switching active hand can be instant
            ((GameRendererExtension) MC.gameRenderer).visor$applyHandPick(activeHand);
        }

        ClientNetworking.sendVRPacket(
                new ActiveHandPayloadToServer(this.activeHand == HandType.MAIN)
        );
    }

    public void onGameLoopStart(){
        this.roomPose.updateTracking(
                VRMathUtils.ZERO_VECTOR,
                1.0f, 0.0f
        );
    }

    public void preTick() {

        this.prevPose.copyFrom(
                this.pose
        );

        //WORLD SCALE
        float preWorldScale = VRRenderState.getSceneType().isMainMenu()
                ? 1.0f
                : VRClientSettings.getWorldScale();

        this.pose.updateTracking(
                pose.getOrigin(),
                preWorldScale,
                this.pose.getRotationY()
        );

        var historyEntry = new LocalPlayerPose(this, PlayerPoseType.ROOM);
        historyEntry.copyFrom(roomPose);
        poseHistoryRoom.addEntry(historyEntry);

        historyEntry = new LocalPlayerPose(this, PlayerPoseType.PREV_TICK);
        historyEntry.copyFrom(prevPose);
        poseHistoryTick.addEntry(historyEntry);

    }


    public void tickPlayer(LocalPlayer player) {
        isTicking = true;
        movePlayerInRoom(player);
        setRotationY(rotationYRaw);
        try {
            var tasks = ClientContext.visor.getTaskRegistry().getPlayerTick();

            for (VisorTask task : tasks) {
                if (task.isEnabledAndActive(player)) {
                    task.run(player);
                } else {
                    task.clear(player);
                }
            }

        } catch (Throwable e) {
            VisorState.destroyVRWithErrorScreen(e);
        }
    }

    public void postTick() {
        this.pose.updateModifiers(
                pose.getOrigin(),
                pose.getWorldScale(),
                rotationYRaw
        );

        this.updatePlayerLook(MC.player, PlayerPoseType.TICK);
        this.overlayFocused = MC.screen != null || ClientContext.cursorHandler.isAnyHandFocused(false);

        ClientNetworking.sendVRPlayerState();

        isTicking = false;
        rotationYRaw = pose.getRotationY();
    }



    public void preRender(float partialTicks) {

        //Interpolated Rotation
        float rotationPre = this.prevPose.getRotationY();
        float rotationPost = this.pose.getRotationY();
        float rotationDelta = Math.abs(rotationPost - rotationPre);

        if (rotationDelta > Math.PI) {
            if (rotationPost > rotationPre) {
                rotationPre = (float) ( rotationPre + (Math.PI * 2));
            } else {
                rotationPost = (float) ( rotationPost + (Math.PI * 2));
            }
        }
        float rotationPartial = rotationPost
                * partialTicks + rotationPre * (1.0f - partialTicks);

        //Interpolated Origin
        var preTickOrigin = this.prevPose.getOrigin();
        var postTickOrigin = this.pose.getOrigin();

        Vector3fc originPartial = new Vector3f(
                preTickOrigin.x()
                        + (postTickOrigin.x() - preTickOrigin.x())
                        * partialTicks,
                preTickOrigin.y()
                        + (postTickOrigin.y() - preTickOrigin.y())
                        * partialTicks,
                preTickOrigin.z()
                        + (postTickOrigin.z() - preTickOrigin.z())
                        * partialTicks
        );

        //Interpolated World Scale
        float preTickWorld = this.prevPose.getWorldScale();
        float postTickWorld = this.pose.getWorldScale();
        float worldScalePartial = postTickWorld * partialTicks
                + preTickWorld * (1.0f - partialTicks);

        //Applying
        this.renderPose.updateTracking(
                originPartial,
                worldScalePartial,
                rotationPartial
        );



    }


    private void movePlayerInRoom(LocalPlayer player){
        if(player == null
                || player.isShiftKeyDown()
                || player.isSleeping()
                || !player.isAlive()){
            return;
        }
        var event = new InRoomMoveVREvent();
        VisorAPI.eventBus().callEvent(event);
        if(event.isCanceled()){
            return;
        }

        var headPivot = pose.getHeadPivot();

        float playerHalfWidth = player.getBbWidth() / 2f;
        float playerHeight = player.getBbHeight();

        Vec3 newPos = new Vec3(
                headPivot.x(),
                player.getY(),
                headPivot.z()
        );

        // Create a collision bounding box at the destination position.
        AABB collisionBox = new AABB(
                newPos.x() - playerHalfWidth,
                newPos.y(),
                newPos.z() - playerHalfWidth,
                newPos.x() + playerHalfWidth,
                newPos.y() + playerHeight,
                newPos.z() + playerHalfWidth
        );


        // If there is no collision at the destination,
        // update the player's position
        if (MC.level.noCollision(player, collisionBox)) {
            //avoid using player.setPos() since it is overridden by Visor
            player.setPosRaw(newPos.x, newPos.y, newPos.z);
            player.setBoundingBox(collisionBox);
            player.fallDistance = 0.0F;
            return;
        }

        boolean smartBlocked = CommonUtils.hasInteractableBlock(
                MC.level,
                collisionBox,
                Mth.floor(collisionBox.minY)
        );

        boolean canAutoClimb = (VRClientSettings.isWalkUpEnabled()
                && ((LocalPlayerExtension) player).visor$getJumpFactor() == 1.0F
                && !smartBlocked);

        if (canAutoClimb && player.fallDistance == 0.0F) {
            // Reduce the collision box width for climbing checks.
            float climbShrink = player.getDimensions(player.getPose()).width() * 0.45F;
            double climbShrinkHalfWidth = playerHalfWidth - climbShrink;

            AABB collisionBoxClimb = new AABB(
                    newPos.x - climbShrinkHalfWidth,
                    collisionBox.minY,
                    newPos.z - climbShrinkHalfWidth,
                    newPos.x + climbShrinkHalfWidth,
                    collisionBox.maxY,
                    newPos.z + climbShrinkHalfWidth
            );

            // If the adjusted box is still collision-free, do not perform a climb.
            if (MC.level.noCollision(player, collisionBoxClimb)) {
                return;
            }


            // Attempt to move upward in small increments until a collision-free space is found.
            for (int i = 0; i <= 10; ++i) {
                collisionBox = collisionBox.move(0.0D, 0.1D, 0.0D);

                if (MC.level.noCollision(player, collisionBox)) {
                    player.setPosRaw(
                            newPos.x(),
                            collisionBox.minY,
                            newPos.z()
                    );
                    player.setBoundingBox(collisionBox);
                    var newRoomOrigin = pose.getOrigin().add(
                            0.0f, 0.1f * (i + 1), 0.0f,
                            new Vector3f()
                    );
                    ClientContext.localPlayer.setOrigin(
                            newRoomOrigin.x,
                            newRoomOrigin.y,
                            newRoomOrigin.z,
                            false
                    );

                    player.fallDistance = 0.0F;
                    ((LocalPlayerExtension) MC.player).visor$stepSound(
                            BlockPos.containing(player.position()),
                            player.position()
                    );
                    break;
                }


            }
        }
    }

    public void updatePlayerLook(LocalPlayer player, PlayerPoseType stage) {
        if (player == null) {
            return;
        }
        LocalPlayerPose data = getPoseData(stage);

        if (player.isPassenger()) {
            var vehicleLookDir = TaskVehicle.getVehicleLookDirection(player);

            if (vehicleLookDir != null) {
                player.setXRot((float) Math.toDegrees(
                        Math.asin(-vehicleLookDir.y() / vehicleLookDir.length()))
                );
                player.setYRot((float) Math.toDegrees(
                        Mth.atan2(-vehicleLookDir.x(), vehicleLookDir.z()))
                );
                player.setYHeadRot(player.getYRot());
            }
            return;
        }
        if (player.isBlocking()) {
            HandType blockHand = player.getUsedItemHand() == InteractionHand.MAIN_HAND
                    ? HandType.MAIN
                    : HandType.OFFHAND;
            visor$applyPoseLook(player, data.getHand(blockHand));
            return;
        }

        if (VRClientSettings.isCompatibleLookDirection()
                && player.isUsingItem()) {
            HandType usingHand = player.getUsedItemHand() == InteractionHand.MAIN_HAND
                    ? HandType.MAIN
                    : HandType.OFFHAND;
            visor$applyPoseLook(player, data.getHand(usingHand));
            return;
        }

        if (player.isSprinting()
                && (player.input.jumping || MC.options.keyJump.isDown())
                || player.isFallFlying()
                || player.isSwimming()
                && player.zza > 0.0F) {

            VRPose rotationElement = getRotationElement(data.getType());
            visor$applyPoseLook(player, rotationElement);
            return;
        }

        Vec3 crossVec = ((GameRendererExtension) MC.gameRenderer).visor$getCrossVec();
        if (!VRClientSettings.isCompatibleLookDirection()
                && crossVec != null) {
            visor$applyVectorLook(
                    player,
                    crossVec.subtract(player.getEyePosition(1.0F)).normalize()
            );
            return;
        }

        visor$applyPoseLook(player, data.getHmd());
    }

    public void recenterOrigin(@NotNull Entity cameraEntity,
                               boolean reset) {


        var headPivot = this.pose.getHeadPivot()
                .sub(pose.getOrigin(), new Vector3f());

        //we want head pivot to be the center,
        // so,
        // we sub it to compensate initial room position of pose data
        float x = (float) (cameraEntity.getX() - headPivot.x());
        float z = (float) (cameraEntity.getZ() - headPivot.z());
        float y = (float) (cameraEntity.getY());
        if (cameraEntity instanceof LocalPlayerExtension p) {
            y += (float) p.visor$getRoomYOffset();
        }
        this.setOrigin(x, y, z, reset);
    }



    public void setOrigin(float x, float y, float z,
                          boolean reset) {

        var newOrigin = new Vector3f(x, y, z);
        if (reset) {
            this.prevPose.resetOrigin(newOrigin);
        }

        this.pose.updateModifiers(
                newOrigin,
                pose.getWorldScale(),
                pose.getRotationY()
        );
    }

    public void setRotationY(float newValue) {
        rotationYRaw = newValue % ((float) Math.PI * 2);
    }

    public float getRotationY(){
        return rotationYRaw;
    }

    public float getWorldScale(){
        return this.pose.getWorldScale();
    }

    @Override
    public @Nullable LocalPlayer getMcPlayer() {
        return MC.player;
    }

    @Override
    public float getGunAngle() {
        return ClientContext.rawPoseHandler.getGunAngle();
    }

    @Override
    public int getOffhandSlot() {
        return TaskHotBar.getInstance().getSlotOffhand();
    }


    @Override
    public RawHmd getRawHmd() {
        return ClientContext.rawPoseHandler.getHmdData();
    }

    @Override
    public RawController getRawController(@NotNull HandType type) {
        return ClientContext.rawPoseHandler.getControllerData(type);
    }

    @Override
    public RawTrackers getRawTrackers() {
        return ClientContext.rawPoseHandler.getTrackersData();
    }

    @Override
    public RawHands getRawHands() {
        return ClientContext.rawPoseHandler.getHandsData();
    }

    @Override
    public @NotNull AtumVRTreadmillView getTreadmill() {
        if (VisorState.get().isNotActive()) {
            return AtumVRTreadmillView.EMPTY;
        }
        return ClientContext.visor.getVrProvider()
                .getInputHandler().getVRTreadmill();
    }

    @Override
    public @NotNull AtumVRBodyHaptics getBodyHaptics() {
        if (VisorState.get().isNotActive()) {
            return AtumVRBodyHaptics.EMPTY;
        }
        return ClientContext.visor.getVrProvider()
                .getInputHandler().getVRBodyHaptics();
    }

    @Override
    public void setOffhandSlot(int slot) {
        if (!VRServerSettings.isTwoHandedVR()) {
            return;
        }
        if (slot != TaskHotBar.NOT_SELECTED
                && (slot < 0 || slot >= 9)) {
            return;
        }
        TaskHotBar.getInstance().setOffhandSlot(slot);
    }


    @Override
    public @NotNull VRPose getRotationElement(@NotNull PlayerPoseType poseType){
        VRPlayerPoseClient playerPose = getPoseData(poseType);
        return switch (VRClientSettings.getRotationMode()) {
            case MAIN_HAND -> playerPose.getHand(
                    HandType.MAIN
            );
            case HMD ->  playerPose.getHmd();
            default -> playerPose.getHand(HandType.OFFHAND);

        };

    }

    @Override
    public @NotNull LocalPlayerPose getPoseData(@NotNull PlayerPoseType stage) {
        return switch (stage){
            case PREV_TICK -> prevPose;
            case TICK -> pose;
            case RENDER -> renderPose;
            default -> roomPose;
        };
    }

    @Override
    public float getFullHeight() {
        return VRClientSettings.getFullHeight();
    }



    @Override
    public boolean isLeftHanded() {
        return VRClientSettings.isLeftHanded();
    }

    @Override
    public boolean isCrawling() {
        return TaskRoomCrawl.getInstance().isCrawling();
    }

    @Override
    public boolean isClimbing() {
        return TaskRoomClimb.getInstance().isGrabbed();
    }

    @Override
    public boolean isClimbing(@NotNull HandType handType) {
        return TaskRoomClimb.getInstance().isGrabbed(handType);
    }


    public String toString() {
        return ("""
            VRLocalPlayer:
                room pose: %s
                previous pose: %s
                pose: %s
                render pose: %s"""
        ).formatted(
                this.roomPose,
                this.prevPose,
                this.pose,
                this.renderPose
        );
    }

    private void visor$applyPoseLook(@NotNull LocalPlayer player,
                                     @NotNull VRPose pose) {
        player.setYRot(pose.getYawDegrees());
        player.setYHeadRot(player.getYRot());
        player.setXRot(-pose.getPitchDegrees());
    }

    private void visor$applyVectorLook(@NotNull LocalPlayer player,
                                       @NotNull Vec3 view) {
        double length = view.length();
        if (length < 1.0E-6D) {
            return;
        }

        double normalizedY = Mth.clamp(-view.y / length, -1.0D, 1.0D);
        float pitch = (float) Math.toDegrees(Math.asin(normalizedY));
        float yaw = (float) Math.toDegrees(Mth.atan2(-view.x, view.z));

        player.setXRot(pitch);
        player.setYRot(yaw);
        player.setYHeadRot(yaw);
    }



}
