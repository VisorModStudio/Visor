package org.vmstudio.visor.core.client.tasks.types.movement;

import lombok.Getter;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.events.AllowClientFeatureVREvent;
import org.vmstudio.visor.api.client.events.InRoomMoveVREvent;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.tasks.RegisterVisorTask;
import org.vmstudio.visor.api.client.tasks.TaskType;
import org.vmstudio.visor.api.client.tasks.VisorTask;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.eventbus.listener.VREventHandler;
import org.vmstudio.visor.api.common.eventbus.listener.VREventListener;
import org.vmstudio.visor.api.common.network.toserver.ClimbingPayloadToServer;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.network.ClientNetworking;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.extensions.client.entity.LocalPlayerExtension;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.EnumMap;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@RegisterVisorTask
public class TaskRoomClimb extends VisorTask
        implements VREventListener {
    private static final String ID = "movement_room_climb";

    @Getter
    private static TaskRoomClimb instance;

    private static final double HAND_DIRECTION_OFFSET = 0.2D;
    private static final double HAND_DISTANCE_THRESHOLD = 0.5D;
    private static final int HAPTIC_PULSE = 2000;
    private static final double COLLISION_BOX_OFFSET = 0.1D;

    private final EnumMap<Direction, AABB> faces = new EnumMap<>(Direction.class);

    private final EnumMap<HandType, HandClimbState> handStates = new EnumMap<>(HandType.class);

    @Getter
    private @Nullable HandType anchoredHand = null;

    private boolean gravityDisabled = false;
    private boolean anchoredThisTick = false;


    public TaskRoomClimb(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;

        for (HandType hand : HandType.values()) {
            handStates.put(hand, new HandClimbState());
        }

        AABB northFaceBox = new AABB(
                COLLISION_BOX_OFFSET, 0.0D, 0.9D,
                0.9D, 1.0D, 1.1D
        );
        AABB southFaceBox = new AABB(
                COLLISION_BOX_OFFSET, 0.0D, -0.1D,
                0.9D, 1.0D, 0.1D
        );
        AABB westFaceBox = new AABB(
                0.9D, 0.0D, COLLISION_BOX_OFFSET,
                1.1D, 1.0D, 0.9D
        );
        AABB eastFaceBox = new AABB(
                -COLLISION_BOX_OFFSET, 0.0D, COLLISION_BOX_OFFSET,
                COLLISION_BOX_OFFSET, 1.0D, 0.9D
        );

        faces.put(Direction.NORTH, northFaceBox);
        faces.put(Direction.SOUTH, southFaceBox);
        faces.put(Direction.WEST, westFaceBox);
        faces.put(Direction.EAST, eastFaceBox);

        VisorAPI.eventBus().registerListener(owner,this);
    }

    @VREventHandler
    public void onAllowClientFeaturesEvent(AllowClientFeatureVREvent event){
        if(event.getFeature() == ClientFeature.AIM_EFFECTS
                && isGrabbed(ClientContext.localPlayer.getActiveHand())){
            event.setCanceled(true);
        }
    }
    @VREventHandler
    public void onRoomMovement(InRoomMoveVREvent event){
        if(isGrabbed()){
            event.setCanceled(true);
        }
    }


    @Override
    public void onRun(LocalPlayer player) {
        anchoredThisTick = false;

        var tickPose = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.TICK);
        // Reset flags for all hands
        for (HandType hand : HandType.values()) {
            HandClimbState state = handStates.get(hand);
            state.isInsideBlock = false;
        }

        Vec3[] handPositions = new Vec3[HandType.values().length];

        Level level = MC.level;

        for (HandType hand : HandType.values()) {
            HandClimbState state = handStates.get(hand);

            Vec3 handPosition = new Vec3((Vector3f) tickPose.getHand(hand).getPosition());
            Vec3 handDirection = new Vec3((Vector3f) tickPose.getHand(hand).getDirection());
            handPositions[hand.ordinal()] = handPosition;

            Vec3 handPosStart = handPosition.subtract(handDirection.scale(HAND_DIRECTION_OFFSET));
            AABB handCollisionBox = new AABB(handPosition, handPosStart);

            BlockPos handBlockPos = BlockPos.containing(handPosition);
            BlockState handBlockState = level.getBlockState(handBlockPos);
            Block handBlock = handBlockState.getBlock();
            VoxelShape handBlockShape = handBlockState.getCollisionShape(level, handBlockPos);

            state.handShape = !handBlockShape.isEmpty() ? handBlockShape.bounds() : null;
            boolean climbableBlock = handBlock instanceof LadderBlock || handBlock instanceof VineBlock;

            if (!climbableBlock) {
                BlockPos blockPosStart = BlockPos.containing(handPosStart);
                BlockState blockStateStart = level.getBlockState(blockPosStart);
                Block blockStart = blockStateStart.getBlock();
                VoxelShape blockShapeStart = blockStateStart.getCollisionShape(level, blockPosStart);

                if (isClimbableBlock(blockStart) && !blockShapeStart.isEmpty()) {
                    handBlockPos = blockPosStart;
                    handBlockState = blockStateStart;
                    handBlock = blockStart;
                    handPositions[hand.ordinal()] = handPosStart;
                    climbableBlock = true;
                    state.handShape = blockShapeStart.bounds();
                }
            }

            updateStateForHand(hand, player, handBlockPos, handBlock, handBlockState, handCollisionBox, handPositions, climbableBlock);
        }

        boolean anchored = handStates.get(HandType.MAIN).isAnchored || handStates.get(HandType.OFFHAND).isAnchored;
        if (!anchored) {
            handleNotAnchored(player, handPositions);
            anchored = handStates.get(HandType.MAIN).isAnchored || handStates.get(HandType.OFFHAND).isAnchored;
        }

        if (!anchored) {
            anchoredHand = null;
            if (gravityDisabled) {
                player.setNoGravity(false);
                gravityDisabled = false;
            }
            return;
        }

        if (!gravityDisabled) {
            player.setNoGravity(true);
            gravityDisabled = true;
        }
        applyAnchorState(player);
    }

    @Override
    public void onClear(LocalPlayer player) {
        for (HandType hand : HandType.values()) {
            HandClimbState state = handStates.get(hand);
            state.isAnchored = false;
        }
        anchoredHand = null;
        player.setNoGravity(false);
    }


    @Override
    public boolean isActive(LocalPlayer player) {
        if (!isEnabled()) return false;
        if (ClientContext.visor.isFeatureDisabled(ClientFeature.MOVEMENT_MODIFIERS)) return false;
        if(!VRClientSettings.isRoomClimbEnabled()) return false;
        if(!VRServerSettings.isRoomClimbingSupported()) return false;
        if (MC.gameMode == null) return false;
        if (player == null || !player.isAlive() || player.isPassenger()) return false;
        // Require zero movement input on X and Z axes
        return player.zza == 0 && player.xxa == 0;
    }


    private void applyAnchorState(Player player) {
        if (anchoredHand == null) return;
        var tickPose = ClientContext.localPlayer.getPoseData(PlayerPoseType.TICK);
        var roomPose = ClientContext.localPlayer.getPoseData(PlayerPoseType.RELATIVE);

        HandClimbState state = handStates.get(anchoredHand);

        Vec3 handTickPos = new Vec3(
                (Vector3f) tickPose.getHand(anchoredHand).getPosition()
        );

        Vec3 anchorTickPos = new Vec3(
                tickPose
                .convertPositionFrom(
                        PlayerPoseType.RELATIVE,
                        state.anchoredPosRoom.toVector3f()
                )
        );
        Vec3 delta = handTickPos.subtract(anchorTickPos);

        state.anchoredPosRoom = new Vec3(
                (Vector3f) roomPose
                .getHand(anchoredHand).getPosition()
        );

        // Preserve horizontal movement if an anchor update occurred this tick
        player.setDeltaMovement(
                anchoredThisTick ? player.getDeltaMovement().x : 0,
                0.0D,
                anchoredThisTick ? player.getDeltaMovement().z : 0
        );
        player.fallDistance = 0;

        double playerX = player.getX();
        double playerY = player.getY();
        double playerZ = player.getZ();
        double newPlayerX = playerX;
        double newPlayerY = playerY - delta.y;
        double newPlayerZ = playerZ;

        BlockPos anchoredBlockPos = BlockPos.containing(state.anchoredPos);
        Direction anchorFace = state.anchorFace;

        switch (anchorFace) {
            case NORTH, SOUTH -> {
                newPlayerX = playerX - delta.x;
                newPlayerZ = anchoredBlockPos.getZ() + 0.5F;
                newPlayerZ += (1.0 - Math.min(ClientContext.localPlayer.getWorldScale(), 1.0))
                        * (anchorFace == Direction.NORTH ? 0.5 : -0.5);
            }
            case EAST, WEST -> {
                newPlayerZ = playerZ - delta.z;
                newPlayerX = anchoredBlockPos.getX() + 0.5F;
                newPlayerX += (1.0 - Math.min(ClientContext.localPlayer.getWorldScale(), 1.0))
                        * (anchorFace == Direction.WEST ? 0.5 : -0.5);
            }
        }

        double headPivotRoomY = roomPose.getHeadPivot().y();
        double anchorHandRoomY = roomPose.getHand(anchoredHand).getPosition().y();

        // If the hand is low relative to the head, try to push the player out of collision
        if (state.anchoredShape != null
                && anchorHandRoomY <= headPivotRoomY / 2.0
                && state.anchoredPos.y > state.anchoredShape.maxY * 0.8 + anchoredBlockPos.getY()) {
            Vec3 hmdDirXZ = new Vec3((Vector3f) tickPose.getHmd()
                    .getDirection())
                    .scale(0.1f)
                    .multiply(1, 0, 0)
                    .normalize()
                    .scale(0.1);
            boolean noCollision = MC.level.noCollision(player,
                    player.getBoundingBox().move(
                            hmdDirXZ.x,
                            state.anchoredShape.maxY + anchoredBlockPos.getY() - player.getY(),
                            hmdDirXZ.z
                    ));
            if (noCollision) {
                newPlayerX = player.getX() + hmdDirXZ.x;
                newPlayerY = state.anchoredShape.maxY + anchoredBlockPos.getY();
                newPlayerZ = player.getZ() + hmdDirXZ.z;
                anchoredHand = null;
                for (HandType hand : HandType.values()) {
                    HandClimbState s = handStates.get(hand);
                    s.isAnchored = false;
                    s.wasInsideBlock = false;
                }
                player.setNoGravity(false);
            }
        }

        // Attempt to resolve collision with new coordinates
        boolean isFree = tryResolveCollision(player, newPlayerX, newPlayerY, newPlayerZ, playerX, playerY, playerZ);
        if (!isFree) {
            player.setPos(playerX, playerY, playerZ);
        }

        if (!MC.isLocalServer()) {
            ClientNetworking.sendVRPacket(new ClimbingPayloadToServer());
            return;
        }
        // Reset fall distance for the server-side player
        for (ServerPlayer serverplayer : MC.getSingleplayerServer().getPlayerList().getPlayers()) {
            if (serverplayer.getUUID().equals(MC.player.getUUID())) {
                serverplayer.fallDistance = 0;
                break;
            }
        }
    }


    private boolean tryResolveCollision(Player player, double newX, double newY, double newZ, double origX, double origY, double origZ) {
        // Try the proposed position first
        player.setPos(newX, newY, newZ);
        if (MC.level.noCollision(player, player.getBoundingBox())) {
            return true;
        }
        // Try alternative adjustments
        for (int i = 0; i < 8; i++) {
            double testX = newX, testY = newY, testZ = newZ;
            switch (i) {
                case 2:
                    testY = origY;
                    break;
                case 3:
                    testZ = origZ;
                    break;
                case 4:
                    testX = origX;
                    break;
                case 5:
                    testX = origX;
                    testZ = origZ;
                    break;
                case 6:
                    testX = origX;
                    testY = origY;
                    break;
                case 7:
                    testY = origY;
                    testZ = origZ;
                    break;
                default:
                    break;
            }
            player.setPos(testX, testY, testZ);
            if (MC.level.noCollision(player, player.getBoundingBox())) {

                return true;
            }
        }
        return false;
    }





    private void handleNotAnchored(Player player, Vec3[] handPositions) {
        for (HandType hand : HandType.values()) {
            HandClimbState state = handStates.get(hand);
            if (!state.isInsideBlock) continue;

            anchoredThisTick = true;
            state.anchoredPos = handPositions[hand.ordinal()];
            state.anchoredPosRoom = new Vec3(
                    (Vector3f) ClientContext.localPlayer
                    .getPoseData(PlayerPoseType.RELATIVE)
                    .getHand(hand).getPosition()
            );
            state.anchoredPosPlayer = player.position();
            anchoredHand = hand;
            state.anchoredShape = state.handShape;
            state.isAnchored = true;

            ClientContext.inputManager.triggerHapticPulseMicroSec(hand, HAPTIC_PULSE);
        }
    }


    private void updateStateForHand(HandType hand, Player player, BlockPos handBlockPos,
                                    Block handBlock, BlockState handBlockState, AABB handCollisionBox,
                                    Vec3[] handPositions, boolean climbableBlock) {
        HandClimbState state = handStates.get(hand);

        if (climbableBlock) {
            updateAnchorFace(state, hand, handBlockPos, handBlock, handBlockState, handCollisionBox);
        } else {
            double distanceToAnchor = state.anchoredPos.subtract(handPositions[hand.ordinal()]).length();
            if (distanceToAnchor > HAND_DISTANCE_THRESHOLD) {
                state.isInsideBlock = false;
            } else {
                BlockPos anchorBlockPos = BlockPos.containing(state.anchoredPos);
                BlockState anchorBlockState = MC.level.getBlockState(anchorBlockPos);
                state.isInsideBlock = state.wasInsideBlock &&
                        (anchorBlockState.getBlock() instanceof LadderBlock ||
                                anchorBlockState.getBlock() instanceof VineBlock);
            }
        }

        // Detach if the hand leaves the climbable block
        if (!state.isInsideBlock && state.isAnchored) {
            state.isAnchored = false;
        }

        // If the hand has just entered a climbable area, set it as the new anchor
        boolean inBlockRecently = !state.wasInsideBlock && state.isInsideBlock;
        boolean canBeAnchored = !state.isAnchored && state.isInsideBlock;
        if (!anchoredThisTick && canBeAnchored && inBlockRecently) {
            anchoredThisTick = true;
            state.anchoredPos = handPositions[hand.ordinal()];
            state.anchoredPosRoom = new Vec3(
                    (Vector3f) ClientContext.localPlayer
                    .getPoseData(PlayerPoseType.RELATIVE)
                    .getHand(hand).getPosition()
            );
            state.anchoredPosPlayer = player.position();
            anchoredHand = hand;
            state.anchoredShape = state.handShape;
            state.isAnchored = true;

            // Detach the other hand
            HandType otherHand = hand == HandType.MAIN
                    ? HandType.OFFHAND : HandType.MAIN;
            handStates.get(otherHand).isAnchored = false;

            ClientContext.inputManager.triggerHapticPulseMicroSec(hand, HAPTIC_PULSE);
            ((LocalPlayerExtension) MC.player).visor$stepSound(handBlockPos, state.anchoredPos);
        }
        state.wasInsideBlock = state.isInsideBlock;
    }


    private void updateAnchorFace(HandClimbState state, HandType hand, BlockPos blockPosHand,
                                  Block blockHand, BlockState blockStateHand, AABB handCollisionBox) {
        if (blockHand instanceof LadderBlock) {
            Direction faceType = blockStateHand.getValue(LadderBlock.FACING);
            AABB faceBox = faces.get(faceType);
            if (handCollisionBox.intersects(faceBox.move(blockPosHand))) {
                state.isInsideBlock = true;
                state.anchorFace = faceType;
            }
            return;
        }

        if (blockHand instanceof VineBlock) {
            state.handShape = new AABB(0, 0, 0, 1, 1, 1);
            AABB faceBox;
            if (blockStateHand.getValue(VineBlock.EAST) && MC.level.getBlockState(blockPosHand.east()).canOcclude()) {
                faceBox = faces.get(Direction.WEST);
                if (handCollisionBox.intersects(faceBox.move(blockPosHand))) {
                    state.isInsideBlock = true;
                    state.anchorFace = Direction.WEST;
                    return;
                }
            }
            if (blockStateHand.getValue(VineBlock.WEST) && MC.level.getBlockState(blockPosHand.west()).canOcclude()) {
                faceBox = faces.get(Direction.EAST);
                if (handCollisionBox.intersects(faceBox.move(blockPosHand))) {
                    state.isInsideBlock = true;
                    state.anchorFace = Direction.EAST;
                    return;
                }
            }
            if (blockStateHand.getValue(VineBlock.NORTH) && MC.level.getBlockState(blockPosHand.north()).canOcclude()) {
                faceBox = faces.get(Direction.SOUTH);
                if (handCollisionBox.intersects(faceBox.move(blockPosHand))) {
                    state.isInsideBlock = true;
                    state.anchorFace = Direction.SOUTH;
                    return;
                }
            }
            if (blockStateHand.getValue(VineBlock.SOUTH) && MC.level.getBlockState(blockPosHand.south()).canOcclude()) {
                faceBox = faces.get(Direction.NORTH);
                if (handCollisionBox.intersects(faceBox.move(blockPosHand))) {
                    state.isInsideBlock = true;
                    state.anchorFace = Direction.NORTH;
                }
            }
        }
    }


    public static boolean isClimbableBlock(Block blockHand) {
        return blockHand instanceof LadderBlock || blockHand instanceof VineBlock;
    }


    public boolean isGrabbed() {
        return handStates.get(HandType.MAIN).isAnchored || handStates.get(HandType.OFFHAND).isAnchored;
    }


    public boolean isGrabbed(HandType hand) {
        return handStates.get(hand).isAnchored;
    }

    @Override
    public @NotNull TaskType getType() {
        return TaskType.VR_PLAYER_TICK;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }



    private static class HandClimbState {
        private boolean isAnchored = false;
        private boolean isInsideBlock = false;
        private boolean wasInsideBlock = false;
        private Direction anchorFace = null;
        private Vec3 anchoredPos = new Vec3(0.0D, 0.0D, 0.0D);
        private Vec3 anchoredPosRoom = new Vec3(0.0D, 0.0D, 0.0D);
        private Vec3 anchoredPosPlayer = new Vec3(0.0D, 0.0D, 0.0D);
        private AABB handShape = null;
        private AABB anchoredShape = null;
    }
}
