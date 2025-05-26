package me.phoenixra.visor.core.client.data;

import lombok.Getter;
import me.phoenixra.visor.api.client.IClientPlayer;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.client.data.IVRClientPose;
import me.phoenixra.visor.api.client.data.IVRPoseElement;
import me.phoenixra.visor.api.client.data.VRPoseStage;
import me.phoenixra.visor.core.client.mcmodified.entity.LocalPlayerModified;
import me.phoenixra.visor.core.client.mcmodified.render.GameRendererModified;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.common.network.client.ClientNetworking;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.phoenixra.visor.core.client.ClientContext;
import static me.phoenixra.visor.core.client.VisorClient.MC;

public class VRClientPlayer implements IClientPlayer {

    private final VRClientPose roomPose;

    private final VRClientPose preTickPose;
    private final VRClientPose postTickPose;
    private final VRClientPose renderPose;

    @Getter
    private Vec3 origin = new Vec3(0.0D, 0.0D, 0.0D);
    @Getter
    private float worldScale = 1.0f;
    @Getter
    private float rotationYaw = 0f;

    @Getter
    private ControllerHand activeHand = ControllerHand.MAIN;

    public VRClientPlayer() {
        this.roomPose = new VRClientPose(VRPoseStage.ROOM, new Vec3(0.0D, 0.0D, 0.0D), VRClientSettings.getWalkMultiplier(), 1.0F, 0.0F);

        this.preTickPose = new VRClientPose(VRPoseStage.PRE_TICK, new Vec3(0.0D, 0.0D, 0.0D), VRClientSettings.getWalkMultiplier(), 1.0F, 0.0F);
        this.postTickPose = new VRClientPose(VRPoseStage.POST_TICK, new Vec3(0.0D, 0.0D, 0.0D), VRClientSettings.getWalkMultiplier(), 1.0F, 0.0F);
        this.renderPose  = new VRClientPose(VRPoseStage.RENDER, new Vec3(0.0D, 0.0D, 0.0D), VRClientSettings.getWalkMultiplier(), 1.0F, 0.0F);
    }


    public void preTick() {

        this.preTickPose.update(
                this.origin,
                VRClientSettings.getWalkMultiplier(),
                this.worldScale,
                rotationYaw
        );


        //WORLD SCALE
        float preWorldScale = VRRenderState.isInMainMenu()
                ? 1.0f
                : VRClientSettings.getWorldScale();

        this.worldScale = preWorldScale;

    }


    public void tickPlayer(LocalPlayer player) {


        //@TODO MOVE TO TRACKERS
        VRClientPose preTickPose = ClientContext.player
                .getPose(VRPoseStage.PRE_TICK);
        Vec3 roomOrigin = ClientContext.player.getOrigin();
        float worldScale = ClientContext.player.getWorldScale();

        Vec3 headPivot = VRPoseHelper.getHeadPivot(
                roomOrigin,
                VRClientSettings.getWalkMultiplier(),
                worldScale,
                preTickPose.getRotationYaw()
        );

        float playerHalfWidth = player.getBbWidth() / 2.0F;
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
            double posY = player.getY();
            player.setPosRaw(headPivot.x, posY, headPivot.z);
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
            for (int i = 0; i <= 16; ++i) {
                collisionBox = collisionBox.move(0.0D, 0.1D, 0.0D);
                if (!MC.level.noCollision(player, collisionBox)) {
                    continue;
                }

                // Update player's position and bounding box.
                player.setPosRaw(headPivot.x, collisionBox.minY, headPivot.z);
                player.setBoundingBox(collisionBox);

                Vec3 newRoomOrigin = roomOrigin.add(0.0, 0.1F * (i + 1), 0.0);
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

    public void postTick() {

        Vec3 hmdPosWorldScaleOld = VRPoseHelper
                .createHmdPose(
                        preTickPose.getOrigin(),
                        VRClientSettings.getWalkMultiplier(),
                        preTickPose.getWorldScale(),
                        preTickPose.getRotationYaw()
                ).getPosition();

        Vec3 hmdPosWorldScaleNow = VRPoseHelper
                .createHmdPose(
                        preTickPose.getOrigin(),
                        VRClientSettings.getWalkMultiplier(),
                        worldScale,
                        preTickPose.getRotationYaw()
                ).getPosition();

        Vec3 hmdWorldScaleDiff = hmdPosWorldScaleNow.subtract(hmdPosWorldScaleOld);
        this.origin = this.origin.subtract(hmdWorldScaleDiff);

        Vec3 headPivot = VRPoseHelper.getHeadPivot(
                origin,
                VRClientSettings.getWalkMultiplier(),
                worldScale,
                preTickPose.getRotationYaw()
        );

        float currentRotation = this.rotationYaw;
        float preTickRotation = this.preTickPose.getRotationYaw();
        this.rotateOriginAround(
                headPivot,
                 preTickRotation - currentRotation
        );

        this.postTickPose.update(
                this.origin,
                VRClientSettings.getWalkMultiplier(),
                this.worldScale,
                currentRotation
        );

        this.updatePlayerLook(MC.player, VRPoseStage.POST_TICK);

        ClientNetworking.sendVRPlayerPose();
    }


    public void preRender(float partialTicks) {

        this.roomPose.update(
                new Vec3(0.0, 0.0, 0.0),
                VRClientSettings.getWalkMultiplier(),
                1.0f, 0.0f
        );

        //Interpolated Rotation
        float rotationPre = this.preTickPose.getRotationYaw();
        float rotationPost = this.postTickPose.getRotationYaw();
        float rotationDelta = Math.abs(rotationPost - rotationPre);

        if (rotationDelta > Math.PI) {
            if (rotationPost > rotationPre) {
                rotationPre = (float) ( rotationPre + (Math.PI * 2));
            } else {
                rotationPost = (float) ( rotationPost + (Math.PI * 2));
            }
        }
        float renderRotation = rotationPost
                * partialTicks + rotationPre * (1.0f - partialTicks);

        //Interpolated Origin
        Vec3 preTickOrigin = this.preTickPose.getOrigin();
        Vec3 postTickOrigin = this.postTickPose.getOrigin();

        Vec3 renderOrigin = new Vec3(
                preTickOrigin.x
                        + (postTickOrigin.x - preTickOrigin.x)
                        * partialTicks,
                preTickOrigin.y
                        + (postTickOrigin.y - preTickOrigin.y)
                        * partialTicks,
                preTickOrigin.z
                        + (postTickOrigin.z - preTickOrigin.z)
                        * partialTicks
        );

        //Interpolated World Scale
        float preTickWorld = this.preTickPose.getWorldScale();
        float postTickWorld = this.postTickPose.getWorldScale();
        float renderWorldScale = postTickWorld * partialTicks
                + preTickWorld * (1.0f - partialTicks);

        //Applying
        this.renderPose.update(
                renderOrigin,
                VRClientSettings.getWalkMultiplier(),
                renderWorldScale, renderRotation
        );



        //PROCESS RENDER_TICKED TRACKERS

    }



    public void updatePlayerLook(LocalPlayer player, VRPoseStage stage) {
        if (player == null) {
            return;
        }
        VRClientPose data = getPose(stage);

        if (player.isPassenger()) {
            //Server-side movement

            return;
        }
        if (player.isBlocking()) {
            //block direction
            if (ClientContext.player.getActiveHand() == ControllerHand.MAIN) {
                player.setYRot(data.getController(ControllerHand.MAIN).getYaw());
                player.setYHeadRot(player.getYRot());
                player.setXRot(-data.getController(ControllerHand.MAIN).getPitch());
            } else {
                player.setYRot(data.getController(ControllerHand.OFFHAND).getYaw());
                player.setYHeadRot(player.getYRot());
                player.setXRot(-data.getController(ControllerHand.OFFHAND).getPitch());
            }
            return;
        }

        if (player.isSprinting()
                && (player.input.jumping || MC.options.keyJump.isDown())
                || player.isFallFlying()
                || player.isSwimming()
                && player.zza > 0.0F) {

            IVRPoseElement rotationElement = getRotationElement(data.getPoseStage());
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
        player.setYRot(data.hmd.getYaw());
        player.setYHeadRot(player.getYRot());
        player.setXRot(-data.hmd.getPitch());
    }

    public void recenterOrigin(@NotNull LocalPlayer player,
                               boolean reset) {


        Vec3 headPivot = this.preTickPose.getHeadPivot();

        Vec3 headOffset = headPivot.subtract(
                this.preTickPose.getOrigin()
        );
        double x = player.getX() - headOffset.x;
        double z = player.getZ() - headOffset.z;
        double y = player.getY()/* + roomYOffset*/;
        this.setOrigin(x, y, z, reset);

    }

    public void rotateOriginAround(Vec3 anchor, float radians) {

        if(radians ==0f){
            return;
        }
        float radSin = Mth.sin(radians);
        float radCos = Mth.cos(radians);
        this.setOrigin(
                radCos
                        * (origin.x - anchor.x)
                        - radSin
                        * (origin.z - anchor.z)
                        + anchor.x, origin.y,
                radSin
                        * (origin.x - anchor.x)
                        + radCos
                        * (origin.z - anchor.z)
                        + anchor.z,
                false
        );
    }


    public void setOrigin(double x, double y, double z,
                          boolean reset) {
        if (reset && this.preTickPose != null) {
            this.preTickPose.resetOrigin(new Vec3(x, y, z));
        }

        this.origin = new Vec3(x, y, z);
    }



    public void setRotationYaw(float rotationYaw) {
        this.rotationYaw = rotationYaw % ((float) Math.PI * 2);
    }




    @Override
    public @Nullable Player getMcPlayer() {
        return MC.player;
    }






    public InteractionHand getActiveInteractHand(){
        return activeHand == ControllerHand.MAIN
                ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    @Override
    public @NotNull IVRPoseElement getRotationElement(@NotNull VRPoseStage stage){
        IVRClientPose playerPose = getPose(stage);
        return switch (VRClientSettings.getRotationMode()) {
            case CONTROLLER_RIGHT -> playerPose.getController(
                    ControllerHand.MAIN
            );
            case HMD ->  playerPose.getHmd();
            default -> playerPose.getController(ControllerHand.OFFHAND);

        };

    }

    @Override
    public @NotNull VRClientPose getPose(@NotNull VRPoseStage stage) {
        return switch (stage){
            case PRE_TICK -> preTickPose;
            case POST_TICK -> postTickPose;
            case RENDER -> renderPose;
            default -> roomPose;
        };
    }

    public String toString() {
        return ("""
            VRClientPlayer:
                origin: %s
                rotation: %.3f
                scale: %.3f
                room pose: %s
                preTick pose: %s
                postTick pose: %s
                render pose: %s"""
        ).formatted(
                this.origin,
                this.rotationYaw,
                this.worldScale,
                this.roomPose,
                this.preTickPose,
                this.postTickPose,
                this.renderPose
        );
    }



}
