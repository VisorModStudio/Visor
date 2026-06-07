package org.vmstudio.visor.extensions.client.render;

import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.core.client.ClientContext;
import org.vmstudio.visor.core.client.render.VRCameraEntityCache;
import org.vmstudio.visor.core.client.render.VRRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public interface GameRendererExtension {


    boolean visor$isVRGuiVisible();

    void visor$setVRGuiVisible(boolean flag);


    void visor$setupCameraEntity(VRPose vrPose);

    default void visor$setupCameraEntityAsVRCamera(){
        VRRenderPass renderPass = VRRenderState.getRenderPass();
        if (renderPass == null) return;
        visor$setupCameraEntity(
                ClientContext.localPlayer
                        .getPoseData(PlayerPoseType.RENDER)
                        .getCameraPose(renderPass)
        );
    }

    void visor$cacheCameraEntity(Entity e);

    void visor$restoreCameraEntity(Entity e);

    void visor$applyCachedCameraEntityPosition(Entity e);

    void visor$setupClipPlanes();

    float visor$getNearClipPlane();

    float visor$getFarClipPlane();


    boolean visor$isInWater();

    boolean visor$isOnFire();

    boolean visor$isInPortal();

    boolean visor$isInBlock();


    /**
     * Returns a 0..1 proximity factor describing how close the current camera
     * eye is to a solid block surface.
     */
    float visor$getBlockProximity();

    void visor$resetProjectionMatrix(float partialTicks);

    Vec3 visor$getCrossVec();

    VRCameraEntityCache visor$getCameraEntityCache();

    Matrix4f visor$getThirdPersonProjection();
}
