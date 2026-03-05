package org.vmstudio.visor.extensions.client.entity;

import org.vmstudio.visor.core.client.render.player.VRPlayerRendererArms;
import org.joml.Quaternionf;

import java.util.Map;

public interface EntityRenderDispatcherExtension {

    Quaternionf visor$getCameraOrientationOffset(float offset);

    Map<String, VRPlayerRendererArms> visor$getArmSkinMap();
}
