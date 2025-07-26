package me.phoenixra.visor.modified.client.render;

import me.phoenixra.visor.core.client.render.VRCameraEntityCache;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public interface GameRendererModified {


    boolean visor$isVRGuiVisible();

    void visor$setVRGuiVisible(boolean flag);


    void visor$setupCameraEntity();

    void visor$cacheCameraEntity(LivingEntity e);

    void visor$restoreCameraEntity(LivingEntity e);


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
