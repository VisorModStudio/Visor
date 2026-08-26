package org.vmstudio.visor.compatibility.sodium.extensions;

import org.vmstudio.visor.compatibility.sodium.FaceUv;

public interface ModelCuboidExtension {
    FaceUv[] visor$faceOverrides();

    void visor$overrideFace(int sodiumFace, FaceUv uv);
}
