package me.phoenixra.visor.core.client.mcmodified.entity;

import me.phoenixra.visor.core.client.render.player.VRPlayerRendererArms;
import org.joml.Quaternionf;

import java.util.Map;

public interface EntityRenderDispatcherVRModified {

    Quaternionf visor$getCameraOrientationOffset(float offset);

    Map<String, VRPlayerRendererArms> visor$getArmSkinMap();
}
