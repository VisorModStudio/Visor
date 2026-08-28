package org.vmstudio.visor.core.client.render;

import com.mojang.math.Axis;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import org.vmstudio.visor.core.client.ClientContext;


public class VRGameCamera extends Camera {

    @Override
    public void setup(@NotNull BlockGetter level,
                      @NotNull Entity entity,
                      boolean thirdPerson,
                      boolean thirdPersonReverse,
                      float partialTicks) {
        if (VRRenderState.getPhase().isVanilla()) {
            super.setup(level, entity, thirdPerson, thirdPersonReverse, partialTicks);
            if (VRRenderState.isSpectatedVRView(entity)) {
                setupSpectatedVR(entity);
            }
        } else {
            setupVR(level, entity);
        }
    }


    @Override
    public void tick() {
        if (VRRenderState.getPhase().isVanilla()) {
            super.tick();
        }
    }


    @Override
    public boolean isDetached() {
        if (VRRenderState.getPhase().isVanilla()) {
            return super.isDetached();
        }
        return VRRenderState.isSelfModelRenderCamera();
    }



    private void setupVR(BlockGetter level, Entity entity) {
        this.initialized = true;
        this.level = level;
        this.entity = entity;

        VRRenderPass renderPass = VRRenderState.getRenderPass();
        VRPose cameraElement = ClientContext.localPlayer
                .getPoseData(PlayerPoseType.RENDER)
                .getCameraPose(renderPass);

        // Position
        this.setPosition(new Vec3(
                (Vector3f) RenderPoseHelper.getCameraPosition(
                        renderPass,
                        ClientContext.localPlayer.getPoseData(PlayerPoseType.RENDER)
                )
        ));

        // Orientation
        this.xRot = -cameraElement.getPitchDegrees();
        this.yRot =  cameraElement.getYawDegrees();

        // Look, Up, Left vectors
        var dir = cameraElement.getDirection();
        var upVec = cameraElement.transformDirection(VRMathUtils.UP_VECTOR);
        var leftVec = cameraElement.transformDirection(VRMathUtils.LEFT_VECTOR);

        this.getLookVector().set(dir.x(), dir.y(), dir.z());
        this.getUpVector().set(upVec.x, upVec.y, upVec.z);
        this.getLeftVector().set(leftVec.x, leftVec.y, leftVec.z);

        // Build rotation quaternion: Yaw then Pitch
        this.rotation().identity()
                .mul(Axis.YP.rotationDegrees(-this.yRot))
                .mul(Axis.XP.rotationDegrees( this.xRot));
    }

    private void setupSpectatedVR(Entity entity) {
        var vrPlayer = VRClientPlayers.getPlayer(entity.getUUID());
        if (vrPlayer == null) {
            return;
        }
        VRPose hmd = vrPlayer.getPoseData(PlayerPoseType.RENDER).getHmd();

        this.setPosition(new Vec3((Vector3f) hmd.getPosition()));

        // Orientation
        this.xRot = -hmd.getPitchDegrees();
        this.yRot =  hmd.getYawDegrees();

        var dir = hmd.getDirection();
        var upVec = hmd.transformDirection(VRMathUtils.UP_VECTOR);
        var leftVec = hmd.transformDirection(VRMathUtils.LEFT_VECTOR);

        this.getLookVector().set(dir.x(), dir.y(), dir.z());
        this.getUpVector().set(upVec.x, upVec.y, upVec.z);
        this.getLeftVector().set(leftVec.x, leftVec.y, leftVec.z);

        this.rotation().identity()
                .mul(Axis.YP.rotationDegrees(-this.yRot))
                .mul(Axis.XP.rotationDegrees( this.xRot));
    }

}
