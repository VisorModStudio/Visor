package org.vmstudio.visor.core.client.render.camera;

//? if <1.21 {
/*import com.mojang.math.Axis;
*///?}
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.vmstudio.visor.core.client.player.VRClientPlayers;
import org.vmstudio.visor.core.client.render.VRRenderState;
import org.vmstudio.visor.core.client.render.helpers.RenderPoseHelper;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
//? if >=26.1 {
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.vmstudio.visor.core.client.VisorState;
import org.vmstudio.visor.core.client.render.helpers.CullFrustumHelper;
import org.vmstudio.visor.extensions.client.render.GameRendererExtension;
//?} elif >=1.21.11 {
/*import net.minecraft.world.level.Level;
*///?} else {
/*import net.minecraft.world.level.BlockGetter;
*///?}
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import org.vmstudio.visor.core.client.ClientContext;
//? if >=26.1 {
import static org.vmstudio.visor.core.client.VisorClientImpl.MC;
//?}


public class VRGameCamera extends Camera {

    //? if >=26.1 {
    @Override
    protected void alignWithEntity(float partialTicks) {
        if (VRRenderState.getPhase().isVanilla()) {
            super.alignWithEntity(partialTicks);
            if (VRRenderState.isSpectatedVRView(this.entity)) {
                setupSpectatedVR(this.entity);
            }
        } else {
            setupVR(this.entity);
        }
    }

    @Override
    protected float calculateFov(float partialTicks) {
        if (VisorState.get().isActive() && VRRenderState.getSceneType().isMainMenu()) {
            return MC.options.fov().get().floatValue();
        }
        return super.calculateFov(partialTicks);
    }

    @Override
    protected Matrix4f createProjectionMatrixForCulling() {
        if (VisorState.get().isNotActive()) {
            return super.createProjectionMatrixForCulling();
        }
        float fov = Math.max(this.getFov(), MC.options.fov().get().intValue());
        return CullFrustumHelper.widenCullProjection(passProjection(fov));
    }

    @Override
    public void extractRenderState(CameraRenderState cameraState, float cameraEntityPartialTicks) {
        super.extractRenderState(cameraState, cameraEntityPartialTicks);
        if (VisorState.get().isActive()) {
            cameraState.projectionMatrix.set(passProjection(this.getFov()));
        }
    }

    @Override
    public Matrix4f getViewRotationMatrix(Matrix4f dest) {
        if (VRRenderState.getPhase().isVanilla()) {
            return super.getViewRotationMatrix(dest);
        }
        return dest.set(RenderPoseHelper.getViewRotation(VRRenderState.getRenderPass()));
    }

    @Override
    public Matrix4f getViewRotationProjectionMatrix(Matrix4f dest) {
        if (VisorState.get().isNotActive()) {
            return super.getViewRotationProjectionMatrix(dest);
        }
        return dest.set(passProjection(this.getFov()))
                .rotate(this.rotation().conjugate(new Quaternionf()));
    }

    private static Matrix4f passProjection(float fov) {
        return ((GameRendererExtension) MC.gameRenderer).visor$passProjection(fov);
    }
    //?} elif >=1.21.11 {
    /*// 1.21.11 takes the Level
    @Override
    public void setup(@NotNull Level level,
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
            this.level = level;
            setupVR(entity);
        }
    }
    *///?} else {
    /*@Override
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
            this.level = level;
            setupVR(entity);
        }
    }
    *///?}


    // outside the vanilla phase only the eye-height smoothing is skipped: GameRenderer.tick runs in the VR_MIRROR phase
    @Override
    public void tick() {
        if (VRRenderState.getPhase().isVanilla()) {
            super.tick();
            return;
        }
        // 1.21.11 samples sky, fog and lightmap values through this probe, an unticked one reads defaults
        //? if >=1.21.11 {
        if (this.entity != null && this.level != null) {
            this.attributeProbe().tick(this.level, this.position());
        }
        //?}
        //? if >=26.1 {
        this.fovModifier = 1.0F;
        this.oldFovModifier = 1.0F;
        //?}
    }


    @Override
    public boolean isDetached() {
        if (VRRenderState.getPhase().isVanilla()) {
            return super.isDetached();
        }
        return VRRenderState.isSelfModelRenderCamera();
    }



    private void setupVR(Entity entity) {
        this.initialized = true;
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

        this.forwards.set(dir.x(), dir.y(), dir.z());
        this.up.set(upVec.x, upVec.y, upVec.z);
        this.left.set(leftVec.x, leftVec.y, leftVec.z);

        applyPoseRotation(cameraElement);
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

        this.forwards.set(dir.x(), dir.y(), dir.z());
        this.up.set(upVec.x, upVec.y, upVec.z);
        this.left.set(leftVec.x, leftVec.y, leftVec.z);

        applyPoseRotation(hmd);
    }

    // 1.21 flipped the Camera basis to -Z forward / -X left, so rotation()
    // is no longer the yaw/pitch
    private void applyPoseRotation(VRPose pose) {
        //? if >=1.21 {
        pose.getRotation().getNormalizedRotation(this.rotation());
        //?} else {
        /*this.rotation().identity()
                .mul(Axis.YP.rotationDegrees(-this.yRot))
                .mul(Axis.XP.rotationDegrees( this.xRot));
        *///?}
        //? if >=26.1 {
        this.matrixPropertiesDirty |= 3;
        //?}
    }

}
