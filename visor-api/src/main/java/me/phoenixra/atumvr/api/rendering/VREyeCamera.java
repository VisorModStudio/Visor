package me.phoenixra.atumvr.api.rendering;


import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public interface VREyeCamera {

    @NotNull Matrix4f getViewMatrix();

    @NotNull Matrix4f getProjectionMatrix();

}
