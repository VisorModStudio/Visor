package me.phoenixra.visor.modified.client.render;

import me.phoenixra.visor.api.client.player.pose.PlayerPoseType;
import me.phoenixra.visor.api.common.player.PoseElement;
import me.phoenixra.visor.core.client.ClientContext;
import me.phoenixra.visor.core.client.render.VRCameraEntityCache;
import me.phoenixra.visor.core.client.render.VRRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public interface GameRendererModified {


    boolean visor$isVRGuiVisible();

    void visor$setVRGuiVisible(boolean flag);


    void visor$setupCameraEntity(PoseElement poseElement);

    default void visor$setupCameraEntityAsVRCamera(){
        visor$setupCameraEntity(
                ClientContext.localPlayer
                .getPoseData(PlayerPoseType.RENDER)
                .getCameraPose(VRRenderState.getCameraType())
        );
    }

    void visor$cacheCameraEntity(Entity e);

    void visor$restoreCameraEntity(Entity e);


    void visor$setupClipPlanes();

    float visor$getNearClipPlane();

    float visor$getFarClipPlane();


    boolean visor$isInWater();

    boolean visor$isOnFire();

    boolean visor$isInPortal();

    float visor$isInBlock();


    void visor$resetProjectionMatrix(float partialTicks);

    Vec3 visor$getCrossVec();

    VRCameraEntityCache visor$getCameraEntityCache();

    Matrix4f visor$getThirdPersonProjection();
}
