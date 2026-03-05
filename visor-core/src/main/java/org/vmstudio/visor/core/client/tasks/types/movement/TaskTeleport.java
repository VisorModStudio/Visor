package org.vmstudio.visor.core.client.tasks.types.movement;

import lombok.Getter;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.ClientFeature;
import org.vmstudio.visor.api.client.events.AllowClientFeatureVREvent;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.tasks.RegisterVisorTask;
import org.vmstudio.visor.api.client.tasks.TaskType;
import org.vmstudio.visor.api.client.tasks.VisorTask;
import org.vmstudio.visor.api.common.HandType;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.eventbus.listener.VREventHandler;
import org.vmstudio.visor.api.common.eventbus.listener.VREventListener;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.api.server.VRServerSettings;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.settings.VRClientSettings;
import org.vmstudio.visor.core.client.settings.options.enums.MovementMode;
import org.vmstudio.visor.extensions.client.entity.LocalPlayerExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static org.vmstudio.visor.core.client.VisorClientImpl.MC;

@RegisterVisorTask
public class TaskTeleport extends VisorTask implements VREventListener {

    private static final String ID = "movement_teleport";

    private static final int MAX_ARC_STEPS = 50;
    private static final float MAX_ENERGY = 100f;

    @Getter
    private static TaskTeleport instance;

    private final Vec3[] arcPoints = new Vec3[MAX_ARC_STEPS];

    private Vec3 destination;

    @Getter
    private double distance;
    @Getter
    private int arcSteps;
    @Getter
    private float energy;
    @Getter
    private boolean arcActive;

    @Getter
    private boolean pressedMainHand;
    @Getter
    private boolean pressedOffhand;

    /**
     * Which controller is used for teleport aiming.
     */
    @Getter
    private HandType usingHand = HandType.OFFHAND;

    private boolean pressedMainHandPre;
    private boolean pressedOffhandPre;

    public TaskTeleport(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
        VisorAPI.eventBus().registerListener(owner, this);
    }

    @VREventHandler
    public void onAllowClientFeaturesEvent(AllowClientFeatureVREvent event) {
        if (!isAiming()) return;

        if(event.getFeature() == ClientFeature.AIM_EFFECTS
                && ClientContext.localPlayer.getActiveHand() == usingHand){
            event.setCanceled(true);
        }
        if (event.getFeature() == ClientFeature.MOVEMENT_MODIFIERS) {
            event.setCanceled(true);
        }
    }

    /* ---------------------------------------------------------------------- */
    /* Task lifecycle                                                         */
    /* ---------------------------------------------------------------------- */

    @Override
    public void onRun(LocalPlayer player) {
        regenerateEnergy();

        boolean teleportTriggered = updateTeleportState();

        if (usingHand != null) {
            return;
        }

        if (teleportTriggered && destination != null) {
            performTeleport(player);
        }
    }

    @Override
    protected void onClear(@Nullable LocalPlayer player) {
        destination = null;
        arcSteps = 0;
        arcActive = false;
        pressedMainHand = false;
        pressedOffhand = false;
        usingHand = HandType.OFFHAND;
        distance = 0.0;
    }

    @Override
    public boolean isActive(LocalPlayer player) {
        if (ClientContext.visor.isFeatureDisabled(ClientFeature.INPUT_MOVEMENT)) return false;
        if (VRClientSettings.getMoveMode(player) != MovementMode.TELEPORT) return false;
        if(TaskRoomClimb.getInstance().isGrabbed()) return false;
        if (player == null || !player.isAlive() || player.isPassenger()) return false;
        return !player.isSleeping();
    }

    /* ---------------------------------------------------------------------- */
    /* Input handling                                                         */
    /* ---------------------------------------------------------------------- */

    public void updateInputState(@Nullable HandType usingHand,
                             Vector2f joystickPos) {
        if(usingHand == HandType.OFFHAND){
            if (!pressedOffhandPre) {
                pressedOffhandPre = joystickPos.y > 0.7f;
            } else {
                pressedOffhandPre = hasJoystickMovement(joystickPos);
            }
        } else if (usingHand == HandType.MAIN) {
            if (!pressedMainHandPre) {
                pressedMainHandPre = joystickPos.y > 0.7f;
            } else {
                pressedMainHandPre = hasJoystickMovement(joystickPos);
            }
        }else{
            pressedOffhand = false;
            pressedMainHand = false;
        }
    }

    private boolean updateTeleportState() {
        boolean teleportTriggered = false;
        boolean ignoreMainPress = false;
        this.pressedMainHand = pressedMainHandPre;
        this.pressedOffhand = pressedOffhandPre;
        this.usingHand = determineUsingHand();

        // If a hand is pressed, we are aiming
        if (usingHand != null) {
            arcActive = true;
        } else {
            // No hand pressed, if we were aiming before, teleport is triggered
            if (arcActive) {
                teleportTriggered = true;
            }
            arcActive = false;
        }

        return teleportTriggered;
    }
    private HandType determineUsingHand() {
        if (pressedMainHand) {
            return HandType.MAIN;
        }
        if (pressedOffhand) {
            return HandType.OFFHAND;
        }
        return null;
    }

    private static boolean hasJoystickMovement(Vector2f joystickPos) {
        return Math.abs(joystickPos.x) > 0.1f
                || Math.abs(joystickPos.y) > 0.1f;
    }





    /* ---------------------------------------------------------------------- */
    /* Teleport execution / energy                                            */
    /* ---------------------------------------------------------------------- */

    private void regenerateEnergy() {
        if (energy < MAX_ENERGY) {
            energy++;
        }
    }

    private void performTeleport(LocalPlayer player) {
        distance = destination.distanceTo(player.position());

        LocalPlayerExtension modified = (LocalPlayerExtension) player;
        modified.visor$setTeleported(true);

        player.moveTo(destination);
        onTeleportEffects(player);

        ((LocalPlayerExtension) MC.player)
                .visor$stepSound(BlockPos.containing(destination), destination);
    }

    private void onTeleportEffects(LocalPlayer player) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.player.fallDistance = 0;

        if (!VRClientSettings.isLimitedSurvivalTeleport()) {
            return;
        }

        if (minecraft.gameMode.hasMissTime()) {
            // cost increases with distance
            energy -= (float) (distance * 4.0);
        }

        // @TODO server?
        minecraft.player.causeFoodExhaustion(
                (float) (distance / 16.0 * 1.2f)
        );
    }

    /* ---------------------------------------------------------------------- */
    /* Arc / destination calculation                                          */
    /* ---------------------------------------------------------------------- */

    private void updateTeleportArc(LocalPlayer player, HandType usedHand) {
        var renderPose = ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER);
        var hand = renderPose.getHand(usedHand);

        // starting position
        var handPos = hand.getPosition();
        arcPoints[0] = new Vec3(handPos.x(), handPos.y(), handPos.z());
        arcSteps = 1;

        Vector3f gravity = hand
                .getRotation()
                .rotateZ(hand.getRoll(), new Matrix4f())
                .transformDirection(VRMathUtils.DOWN_VECTOR, new Vector3f())
                .mul(0.098f);

        final float speed = 0.5F;
        Vector3f velocity = hand.getDirection().mul(speed, new Vector3f());

        Vec3 startPos = new Vec3(handPos.x(), handPos.y(), handPos.z());

        // Generate arc points
        for (int step = arcSteps; step < MAX_ARC_STEPS && (step * 4.0f) <= energy; ++step) {
            Vec3 newPosition = startPos.add(velocity.x, velocity.y, velocity.z);

            BlockHitResult hitResult = MC.level.clip(new ClipContext(
                    startPos,
                    newPosition,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.ANY,
                    MC.player
            ));

            if (hitResult.getType() == HitResult.Type.MISS) {
                startPos = newPosition;
                arcPoints[step] = newPosition;
                arcSteps = step + 1;
                velocity = velocity.add(gravity);
                continue;
            }

            // Hit something
            arcPoints[step] = hitResult.getLocation();
            arcSteps = step + 1;
            updateDestination(player, hitResult);

            if (!isTeleportDestinationAllowed(player)) {
                destination = null;
                distance = 0.0;
                break;
            }

            // distance from player to destination
            Vec3 destVector = MC.player.position().subtract(destination);
            distance = destVector.length();
            break;
        }

        // If we have a destination but the last arc point is too far, smooth the arc
        if (destination != null && arcSteps > 0) {
            Vec3 lastArcPoint = arcPoints[arcSteps - 1];
            if (lastArcPoint.distanceTo(destination) > 0.3) {
                generateArcPointsManually(handPos, destination);
            }
        }
    }

    private boolean isTeleportDestinationAllowed(LocalPlayer player) {
        if (destination == null) {
            return false;
        }

        Vec3 destVector = MC.player.position().subtract(destination);
        double verticalDelta = destVector.y;
        double horizDistance = Math.sqrt(destVector.x * destVector.x + destVector.z * destVector.z);

        boolean isAllowed = !MC.player.isShiftKeyDown() || verticalDelta < 0.2;

        if (!MC.player.getAbilities().mayfly && VRClientSettings.isLimitedSurvivalTeleport()) {
            double downLimit = VRServerSettings.getTeleportDownLimit();
            double upLimit = VRServerSettings.getTeleportUpLimit();
            double forwardLimit = VRServerSettings.getTeleportForwardLimit();

            LocalPlayerExtension modified = (LocalPlayerExtension) player;

            boolean exceedsDownLimit = downLimit > 0
                    && verticalDelta > downLimit + 0.2;
            boolean exceedsUpLimit = upLimit > 0
                    && -verticalDelta > upLimit * modified.visor$getJumpFactor() + 0.2;
            boolean exceedsForwardLimit = forwardLimit > 0
                    && horizDistance > forwardLimit * modified.visor$getSpeedFactor() + 0.2;

            if (exceedsDownLimit || exceedsUpLimit || exceedsForwardLimit) {
                isAllowed = false;
            }
        }

        return isAllowed;
    }

    private void generateArcPointsManually(Vector3fc handPos, Vec3 destination) {
        int numPoints = arcSteps;
        if (numPoints <= 1) {
            return;
        }

        double midX = (handPos.x() + destination.x) / 2.0;
        double midZ = (handPos.z() + destination.z) / 2.0;

        double midY = Math.max(handPos.y(), destination.y) + 1.5;
        Vec3 controlPoint = new Vec3(midX, midY, midZ);

        for (int i = 0; i < numPoints; i++) {
            double t = i / (double) (numPoints - 1);
            double oneMinusT = 1 - t;

            double x = oneMinusT * oneMinusT * handPos.x()
                    + 2 * oneMinusT * t * controlPoint.x
                    + t * t * destination.x;
            double y = oneMinusT * oneMinusT * handPos.y()
                    + 2 * oneMinusT * t * controlPoint.y
                    + t * t * destination.y;
            double z = oneMinusT * oneMinusT * handPos.z()
                    + 2 * oneMinusT * t * controlPoint.z
                    + t * t * destination.z;

            arcPoints[i] = new Vec3(x, y, z);
        }
    }

    /* ---------------------------------------------------------------------- */
    /* Destination update helpers                                             */
    /* ---------------------------------------------------------------------- */

    private void updateDestination(LocalPlayer player, BlockHitResult collision) {
        BlockPos blockPos = collision.getBlockPos();
        BlockState blockState = player.level().getBlockState(blockPos);

        boolean isFluid = !MC.level.getFluidState(blockPos).isEmpty();
        if (isFluid) {
            if (updateFluidDestination(player, collision, blockPos)) {
                return;
            }
        } else if (collision.getDirection() != Direction.UP) {
             if (TaskRoomClimb.isClimbableBlock(blockState.getBlock())) {
                updateClimbableDestination(blockPos, blockState);
                return;
            }
            if (!MC.player.getAbilities().mayfly
                    && VRClientSettings.isLimitedSurvivalTeleport()) {
                return;
            }
        }

        updateSolidDestination(player, collision);
    }

    private void updateSolidDestination(LocalPlayer player, BlockHitResult collision) {
        BlockPos blockBelow = collision.getBlockPos().below();

        for (int i = 0; i < 2; ++i) {
            BlockState blockState = player.level().getBlockState(blockBelow);

            if (blockState.getCollisionShape(MC.level, blockBelow).isEmpty()) {
                blockBelow = blockBelow.above();
                continue;
            }

            double maxY = blockState.getCollisionShape(MC.level, blockBelow).max(Direction.Axis.Y);
            Vec3 basePos = new Vec3(
                    collision.getLocation().x,
                    blockBelow.getY() + maxY,
                    collision.getLocation().z
            );

            Vec3 offset = basePos.subtract(
                    player.getX(),
                    player.getBoundingBox().minY,
                    player.getZ()
            );

            AABB playerShape = player.getBoundingBox().move(offset.x, offset.y, offset.z);

            double extraY = 0;
            Block block = blockState.getBlock();
            if (block == Blocks.SOUL_SAND || block == Blocks.HONEY_BLOCK) {
                extraY = 0.05;
            }

            boolean hasSpaceForPlayer = hasSpaceForPlayer(player, playerShape, extraY);
            if (!hasSpaceForPlayer) {
                Vec3 fallbackPos = Vec3.upFromBottomCenterOf(blockBelow, maxY);
                offset = fallbackPos.subtract(
                        player.getX(),
                        player.getBoundingBox().minY,
                        player.getZ()
                );
                playerShape = player.getBoundingBox().move(offset.x, offset.y, offset.z);
                hasSpaceForPlayer = hasSpaceForPlayer(player, playerShape, extraY);
            }

            if (hasSpaceForPlayer) {
                this.destination = new Vec3(
                        playerShape.getCenter().x,
                        blockBelow.getY() + maxY,
                        playerShape.getCenter().z
                );
                return;
            }

            blockBelow = blockBelow.above();
        }
    }

    private boolean hasSpaceForPlayer(LocalPlayer player, AABB playerShape, double extraY) {
        return MC.level.noCollision(player, playerShape)
                && !MC.level.noCollision(player, playerShape.inflate(0, 0.125 + extraY, 0));
    }

    private void updateClimbableDestination(BlockPos blockPos, BlockState blockState) {
        Vec3 newDestination = new Vec3(
                blockPos.getX() + 0.5,
                blockPos.getY() + 0.5,
                blockPos.getZ() + 0.5
        );

        Block blockBelow = MC.level.getBlockState(blockPos.below()).getBlock();
        if (blockBelow == blockState.getBlock()) {
            newDestination = newDestination.add(0, -1, 0);
        }
        this.destination = newDestination;
    }

    private boolean updateFluidDestination(LocalPlayer player, BlockHitResult collision, BlockPos blockPos) {
        Vec3 basePos = new Vec3(
                collision.getLocation().x,
                blockPos.getY(),
                collision.getLocation().z
        );

        Vec3 offset = basePos.subtract(
                player.getX(),
                player.getBoundingBox().minY,
                player.getZ()
        );

        AABB playerShape = player.getBoundingBox().move(offset.x, offset.y, offset.z);
        boolean hasSpaceForPlayer = MC.level.noCollision(player, playerShape);

        if (!hasSpaceForPlayer) {
            Vec3 fallbackPos = Vec3.atBottomCenterOf(blockPos);
            offset = fallbackPos.subtract(
                    player.getX(),
                    player.getBoundingBox().minY,
                    player.getZ()
            );
            playerShape = player.getBoundingBox().move(offset.x, offset.y, offset.z);
            hasSpaceForPlayer = MC.level.noCollision(player, playerShape);
        }

        if (hasSpaceForPlayer) {
            this.destination = new Vec3(
                    playerShape.getCenter().x,
                    playerShape.minY,
                    playerShape.getCenter().z
            );
            return true;
        }

        return false;
    }

    /* ---------------------------------------------------------------------- */
    /* Static helpers                                                         */
    /* ---------------------------------------------------------------------- */

    public static void updateTeleportDestination(LocalPlayer player) {
        if (instance == null || instance.usingHand == null) return;
        instance.destination = null;

        if (instance.arcActive) {
            instance.updateTeleportArc(player, instance.usingHand);
        }
    }

    public static Vec3 getArcPosInterpolated(float progress) {
        if (instance == null) return new Vec3(0, 0, 0);
        if (instance.arcSteps <= 0 || instance.arcPoints[0] == null) {
            return new Vec3(0, 0, 0);
        }

        if (instance.arcSteps == 1 || progress <= 0.0f) {
            Vec3 start = instance.arcPoints[0];
            return new Vec3(start.x, start.y, start.z);
        }

        if (progress >= 1.0f) {
            Vec3 last = instance.arcPoints[instance.arcSteps - 1];
            return new Vec3(last.x, last.y, last.z);
        }

        float step = progress * (instance.arcSteps - 1);
        int stepIndex = (int) Math.floor(step);
        float stepFactor = step - stepIndex;

        Vec3 a = instance.arcPoints[stepIndex];
        Vec3 b = instance.arcPoints[stepIndex + 1];

        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double dz = b.z - a.z;

        return new Vec3(
                a.x + dx * stepFactor,
                a.y + dy * stepFactor,
                a.z + dz * stepFactor
        );
    }

    public static boolean isAiming() {
        return instance != null && (instance.pressedMainHand || instance.pressedOffhand);
    }

    public static boolean isPressed(HandType hand) {
        if (instance == null) return false;

        return hand == HandType.MAIN
                ? instance.pressedMainHand
                : instance.pressedOffhand;
    }

    public static Vec3 getDestination() {
        return instance == null ? null : instance.destination;
    }

    @Override
    public @NotNull TaskType getType() {
        return TaskType.VR_PLAYER_TICK;
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }
}