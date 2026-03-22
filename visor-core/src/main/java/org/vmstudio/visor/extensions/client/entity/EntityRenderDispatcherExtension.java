package org.vmstudio.visor.extensions.client.entity;

import org.joml.Quaternionf;

import java.util.Map;

public interface EntityRenderDispatcherExtension {

    Quaternionf visor$getCameraOrientationOffset(float scale, float offset);

}
