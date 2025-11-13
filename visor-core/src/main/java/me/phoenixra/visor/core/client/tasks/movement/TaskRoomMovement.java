package me.phoenixra.visor.core.client.tasks.movement;


import lombok.Getter;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.tasks.RegisterVisorTask;
import me.phoenixra.visor.api.client.tasks.TaskType;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.addon.element.ElementPriority;
import me.phoenixra.visor.api.common.addon.VisorAddon;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.data.PoseDataImpl;
import me.phoenixra.visor.modified.client.entity.LocalPlayerModified;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

//Task for movement of player in room
@RegisterVisorTask
public class TaskRoomMovement extends VisorTask {
    private static final String ID = "room_movement";
    @Getter
    private static TaskRoomMovement instance;

    private int freezeMovementTimer;

    public TaskRoomMovement(@NotNull VisorAddon owner) {
        super(owner);
        instance = this;
    }

    @Override
    protected void onRun(@Nullable LocalPlayer player) {
        if (this.freezeMovementTimer > 0) {
            this.freezeMovementTimer--;
            return;
        }

        //@TODO maybe block this logic when player moves via input?

        PoseDataImpl preTickPose = ClientContext.player
                .getPoseData(PoseDataType.PRE_TICK);
        var origin = ClientContext.player.getOrigin();
        float worldScale = ClientContext.player.getWorldScale();

        var headPivot = preTickPose.createNewHeadPivot(
                origin,
                worldScale
        );

        float playerHalfWidth = player.getBbWidth() / 2f;
        float playerHeight = player.getBbHeight();
        double playerPosY = player.getY();

        // Create a collision bounding box at the destination position.
        AABB collisionBox = new AABB(
                headPivot.x - playerHalfWidth,
                playerPosY,
                headPivot.z - playerHalfWidth,
                headPivot.x + playerHalfWidth,
                playerPosY + playerHeight,
                headPivot.z + playerHalfWidth
        );


        // If there is no collision at the destination,
        // update the player's position
        if (MC.level.noCollision(player, collisionBox)) {
            //avoid using player.setPos() since it is overridden by Visor
            player.setPosRaw(headPivot.x, player.getY(), headPivot.z);
            player.setBoundingBox(collisionBox);
            player.fallDistance = 0.0F;
            return;
        }

        boolean canAutoClimb = (VRClientSettings.isWalkUpEnabled()
                && ((LocalPlayerModified) player).visor$getJumpFactor() == 1.0F);

        if (canAutoClimb && player.fallDistance == 0.0F) {
            Vec3 torso = new Vec3(headPivot.x, playerPosY, headPivot.z);
            // Reduce the collision box width for climbing checks.
            float climbShrink = player.getDimensions(player.getPose()).width * 0.45F;
            double shrunkClimbHalfWidth = playerHalfWidth - climbShrink;

            AABB collisionBoxClimb = new AABB(
                    torso.x - shrunkClimbHalfWidth,
                    collisionBox.minY,
                    torso.z - shrunkClimbHalfWidth,
                    torso.x + shrunkClimbHalfWidth,
                    collisionBox.maxY,
                    torso.z + shrunkClimbHalfWidth
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
                            headPivot.x,
                            collisionBox.minY,
                            headPivot.z
                    );
                    player.setBoundingBox(collisionBox);

                    var newRoomOrigin = origin.add(
                            0.0f, 0.1f * (i + 1), 0.0f,
                            new Vector3f()
                    );
                    ClientContext.player.setOrigin(
                            newRoomOrigin.x,
                            newRoomOrigin.y,
                            newRoomOrigin.z,
                            false
                    );

                    player.fallDistance = 0.0F;
                    ((LocalPlayerModified) MC.player).visor$stepSound(
                            BlockPos.containing(player.position()),
                            player.position()
                    );
                    break;
                }


            }
        }
    }

    @Override
    protected void onClear(@Nullable LocalPlayer player) {

    }

    @Override
    public boolean isActive(@Nullable LocalPlayer player) {
        return player != null
                && !player.isShiftKeyDown()
                && !player.isSleeping()
                && player.isAlive()
                && !ClientContext.player.isMoving();
    }

    @Override
    public @NotNull String getId() {
        return ID;
    }

    @Override
    public @NotNull TaskType getType() {
        return TaskType.VR_PLAYER_TICK;
    }

    @Override
    public @NotNull ElementPriority getPriority() {
        return ElementPriority.HIGHEST;
    }
}
