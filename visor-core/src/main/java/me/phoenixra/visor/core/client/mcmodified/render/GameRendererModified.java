package me.phoenixra.visor.core.client.mcmodified.render;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public interface GameRendererModified {



    void visor$setupVRCameraEntity();

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
}
