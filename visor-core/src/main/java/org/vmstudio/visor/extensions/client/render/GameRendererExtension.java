package org.vmstudio.visor.extensions.client.render;

import org.joml.Matrix4f;

public interface GameRendererExtension {


    boolean visor$isVRGuiVisible();

    void visor$setVRGuiVisible(boolean flag);


    void visor$setupClipPlanes();

    float visor$getNearClipPlane();

    float visor$getFarClipPlane();


    void visor$resetProjectionMatrix(float partialTicks);

    Matrix4f visor$passProjection(float fov);

    Matrix4f visor$getThirdPersonProjection();
}
