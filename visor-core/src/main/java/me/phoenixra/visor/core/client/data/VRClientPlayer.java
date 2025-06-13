package me.phoenixra.visor.core.client.data;

import lombok.Getter;
import me.phoenixra.visor.api.client.ClientPlayer;

import me.phoenixra.visor.api.client.tasks.VisorTask;
import me.phoenixra.visor.api.common.ControllerHand;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.data.PoseType;
import me.phoenixra.visor.core.client.VisorState;
import me.phoenixra.visor.core.client.mcmodified.render.GameRendererModified;
import me.phoenixra.visor.core.client.render.VRRenderState;
import me.phoenixra.visor.core.client.settings.VRClientSettings;
import me.phoenixra.visor.core.common.network.client.ClientNetworking;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import me.phoenixra.visor.core.client.ClientContext;

import static me.phoenixra.visor.core.client.VisorClientImpl.MC;

public class VRClientPlayer implements ClientPlayer {

    private final PoseDataImpl roomPose;

    private final PoseDataImpl preTickPose;
    private final PoseDataImpl postTickPose;
    private final PoseDataImpl renderPose;

    @Getter
    private Vec3 origin = new Vec3(0.0D, 0.0D, 0.0D);
    @Getter
    private float worldScale = 1.0f;
    @Getter
    private float rotationY = 0f;


    @Getter
    private ControllerHand activeHand = ControllerHand.MAIN;

    @Getter
    private Input inputMovement = new Input();

    public VRClientPlayer() {
        this.roomPose = new PoseDataImpl(PoseType.ROOM, new Vec3(0.0D, 0.0D, 0.0D), VRClientSettings.getWalkMultiplier(), 1.0F, 0.0F);

        this.preTickPose = new PoseDataImpl(PoseType.PRE_TICK, new Vec3(0.0D, 0.0D, 0.0D), VRClientSettings.getWalkMultiplier(), 1.0F, 0.0F);
        this.postTickPose = new PoseDataImpl(PoseType.POST_TICK, new Vec3(0.0D, 0.0D, 0.0D), VRClientSettings.getWalkMultiplier(), 1.0F, 0.0F);
        this.renderPose  = new PoseDataImpl(PoseType.RENDER, new Vec3(0.0D, 0.0D, 0.0D), VRClientSettings.getWalkMultiplier(), 1.0F, 0.0F);
    }


    public void preTick() {

        this.preTickPose.update(
                this.origin,
                VRClientSettings.getWalkMultiplier(),
                this.worldScale,
                rotationY
        );

        //WORLD SCALE
        float preWorldScale = VRRenderState.isInMainMenu()
                ? 1.0f
                : VRClientSettings.getWorldScale();

        this.worldScale = preWorldScale;

    }


    public void tickPlayer(LocalPlayer player) {

        try {
            var tasks = ClientContext.visor.getTaskRegistry().getPlayerTick();

            for (VisorTask task : tasks) {
                if (task.isEnabledAndActive(player)) {
                    task.run(player);
                } else {
                    task.clear(player);
                }
            }
            throw new RuntimeException("EXAMPLE EXCEPTION");
        } catch (Throwable e) {
            VisorState.destroyVRWithErrorScreen(e);
        }
    }

    public void postTick() {

        Vec3 hmdPosWorldScaleOld = PoseDataHelper
                .createHmdPose(
                        preTickPose.getOrigin(),
                        VRClientSettings.getWalkMultiplier(),
                        preTickPose.getWorldScale(),
                        preTickPose.getRotationY()
                ).getPosition();

        Vec3 hmdPosWorldScaleNow = PoseDataHelper
                .createHmdPose(
                        preTickPose.getOrigin(),
                        VRClientSettings.getWalkMultiplier(),
                        worldScale,
                        preTickPose.getRotationY()
                ).getPosition();

        Vec3 hmdWorldScaleDiff = hmdPosWorldScaleNow.subtract(hmdPosWorldScaleOld);
        this.origin = this.origin.subtract(hmdWorldScaleDiff);

        Vec3 headPivot = PoseDataHelper.getHeadPivot(
                origin,
                VRClientSettings.getWalkMultiplier(),
                worldScale,
                preTickPose.getRotationY()
        );

        float currentRotation = this.rotationY;
        float preTickRotation = this.preTickPose.getRotationY();
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

        this.updatePlayerLook(MC.player, PoseType.POST_TICK);

        ClientNetworking.sendVRPlayerPose();
    }


    public void preRender(float partialTicks) {

        this.roomPose.update(
                new Vec3(0.0, 0.0, 0.0),
                VRClientSettings.getWalkMultiplier(),
                1.0f, 0.0f
        );

        //Interpolated Rotation
        float rotationPre = this.preTickPose.getRotationY();
        float rotationPost = this.postTickPose.getRotationY();
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



    public void updatePlayerLook(LocalPlayer player, PoseType stage) {
        if (player == null) {
            return;
        }
        PoseDataImpl data = getPose(stage);

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



    public void setRotationY(float rotationY) {
        this.rotationY = rotationY % ((float) Math.PI * 2);
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
    public @NotNull PoseElement getRotationElement(@NotNull PoseType stage){
        PoseData playerPose = getPose(stage);
        return switch (VRClientSettings.getRotationMode()) {
            case CONTROLLER_MAIN -> playerPose.getController(
                    ControllerHand.MAIN
            );
            case HMD ->  playerPose.getHmd();
            default -> playerPose.getController(ControllerHand.OFFHAND);

        };

    }

    @Override
    public @NotNull PoseDataImpl getPose(@NotNull PoseType stage) {
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
                this.rotationY,
                this.worldScale,
                this.roomPose,
                this.preTickPose,
                this.postTickPose,
                this.renderPose
        );
    }



}
