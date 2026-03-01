package org.vmstudio.visor.modified.client.entity;

import org.vmstudio.visor.core.client.render.player.VRPlayerRendererArms;
import org.joml.Quaternionf;

import java.util.Map;

public interface EntityRenderDispatcherVRModified {

    Quaternionf visor$getCameraOrientationOffset(float offset);

    Map<String, VRPlayerRendererArms> visor$getArmSkinMap();
}
