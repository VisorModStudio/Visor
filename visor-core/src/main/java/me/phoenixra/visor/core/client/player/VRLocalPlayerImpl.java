package me.phoenixra.visor.core.client.player;

import lombok.Getter;

import lombok.Setter;
import me.phoenixra.visor.api.client.player.VRLocalPlayer;
import me.phoenixra.visor.api.client.player.pose.PlayerPoseClient;
import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.HandType;
import me.phoenixra.visor.api.common.player.PoseElement;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.player.pose.LocalPlayerPose;
import me.phoenixra.visor.modified.client.entity.LocalPlayerModified;
import me.phoenixra.visor.modified.client.render.GameRendererModified;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.client.tasks.movement.vehicle.TaskRoomVehicle;
import me.phoenixra.visor.core.client.network.ClientNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import me.phoenixra.visor.core.client.ClientContext;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class VRLocalPlayerImpl implements VRLocalPlayer {

    private final LocalPlayerPose roomPose;

    private final LocalPlayerPose prevPose;
    private final LocalPlayerPose pose;
    private final LocalPlayerPose renderPose;


    @Getter
    private HandType activeHand = HandType.MAIN;

    @Getter
    private Vector2f movement = new Vector2f();
    @Getter @Setter
    private boolean moving;


    public VRLocalPlayerImpl() {
        this.roomPose = new LocalPlayerPose(PlayerPoseType.ROOM, VRMathUtils.ZERO_VECTOR, VRClientSettings.getWalkMultiplier(), 1.0F, 0.0F);

        this.prevPose = new LocalPlayerPose(PlayerPoseType.PREV_TICK, VRMathUtils.ZERO_VECTOR, VRClientSettings.getWalkMultiplier(), 1.0F, 0.0F);
        this.pose = new LocalPlayerPose(PlayerPoseType.TICK, VRMathUtils.ZERO_VECTOR, VRClientSettings.getWalkMultiplier(), 1.0F, 0.0F);
        this.renderPose  = new LocalPlayerPose(PlayerPoseType.RENDER, VRMathUtils.ZERO_VECTOR, VRClientSettings.getWalkMultiplier(), 1.0F, 0.0F);
    }

    public void onGameLoopStart(){
        this.roomPose.update(
                VRMathUtils.ZERO_VECTOR,
                VRClientSettings.getWalkMultiplier(),
                1.0f, 0.0f
        );
    }

    public void preTick() {

        this.prevPose.copyFrom(
                this.pose
        );

        //WORLD SCALE
        float preWorldScale = VRRenderState.isInMainMenu()
                ? 1.0f
                : VRClientSettings.getWorldScale();

        this.pose.update(
                this.pose.getOrigin(),
                VRClientSettings.getWalkMultiplier(),
                preWorldScale,
                pose.getRotationY()
        );

    }


    public void tickPlayer(LocalPlayer player) {

        movePlayerInRoom(player);
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

        this.updatePlayerLook(MC.player, PlayerPoseType.TICK);

        ClientNetworking.sendVRPlayerPose();
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
        this.renderPose.update(
                originPartial,
                VRClientSettings.getWalkMultiplier(),
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

        boolean canAutoClimb = (VRClientSettings.isWalkUpEnabled()
                && ((LocalPlayerModified) player).visor$getJumpFactor() == 1.0F);

        if (canAutoClimb && player.fallDistance == 0.0F) {
            // Reduce the collision box width for climbing checks.
            float climbShrink = player.getDimensions(player.getPose()).width * 0.45F;
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
                    ((LocalPlayerModified) MC.player).visor$stepSound(
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
            var vehicleLookDir = TaskRoomVehicle.getVehicleLookDirection(player);

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
            //block direction
            if (ClientContext.localPlayer.getActiveHand() == HandType.MAIN) {
                player.setYRot(data.getHand(HandType.MAIN).getYaw());
                player.setYHeadRot(player.getYRot());
                player.setXRot(-data.getHand(HandType.MAIN).getPitch());
            } else {
                player.setYRot(data.getHand(HandType.OFFHAND).getYaw());
                player.setYHeadRot(player.getYRot());
                player.setXRot(-data.getHand(HandType.OFFHAND).getPitch());
            }
            return;
        }

        if (player.isSprinting()
                && (player.input.jumping || MC.options.keyJump.isDown())
                || player.isFallFlying()
                || player.isSwimming()
                && player.zza > 0.0F) {

            PoseElement rotationElement = getRotationElement(data.getType());
            player.setYRot(rotationElement.getYaw());
            player.setYHeadRot(player.getYRot());
            player.setXRot(-rotationElement.getPitch());
            return;
        }

        if (((GameRendererModified) MC.gameRenderer).visor$getCrossVec() != null) {
            //Look AT the crosshair by default, most compatible with mods.
            Vec3 playerToCrosshair = player.getEyePosition(1)
                    .subtract(((GameRendererModified) MC.gameRenderer)
                            .visor$getCrossVec()); //backwards
            double what = playerToCrosshair.y / playerToCrosshair.length();
            if (what > 1) {
                what = 1;
            }
            if (what < -1) {
                what = -1;
            }
            float pitch = (float) Math.toDegrees(Math.asin(what));
            float yaw = (float) Math.toDegrees(
                    Mth.atan2(playerToCrosshair.x, -playerToCrosshair.z)
            );
            player.setXRot(pitch);
            player.setYRot(yaw);
            player.setYHeadRot(yaw);
            return;
        }

        //use HMD if no other option found
        player.setYRot(data.getHmd().getRotationYCache());
        player.setYHeadRot(player.getYRot());
        player.setXRot(-data.getHmd().getPitch());
    }

    public void recenterOrigin(@NotNull Entity cameraEntity,
                               boolean reset) {


        var headPivot = this.pose.getHeadPivot()
                .sub(pose.getOrigin(), new Vector3f());

        //we want head pivot to be the center,
        // so,
        // we sub it to compensate initial room position of pose elements
        float x = (float) (cameraEntity.getX() - headPivot.x());
        float z = (float) (cameraEntity.getZ() - headPivot.z());
        float y = (float) (cameraEntity.getY());
        if (cameraEntity instanceof LocalPlayerModified p) {
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

        this.pose.update(
                newOrigin,
                pose.getWorldScale(),
                pose.getRotationY()
        );
    }

    public void setRotationY(float newYaw) {
        this.pose.update(
                pose.getOrigin(),
                pose.getWorldScale(),
                newYaw % ((float) Math.PI * 2)
        );
    }




    @Override
    public LocalPlayer getMcPlayer() {
        return MC.player;
    }






    public InteractionHand getActiveInteractHand(){
        return activeHand == HandType.MAIN
                ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    @Override
    public @NotNull PoseElement getRotationElement(@NotNull PlayerPoseType stage){
        PlayerPoseClient playerPose = getPoseData(stage);
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
    public float getHeight() {
        return VRClientSettings.getPlayerHeight();
    }

    @Override
    public boolean isLeftHanded() {
        return VRClientSettings.isLeftHanded();
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



}
