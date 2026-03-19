package org.vmstudio.visor.compatibility.sodium.extensions;

public interface ModelCuboidExtension {

    float[][] visor$getOverrides();

    void visor$addOverrides(int overrideFaceIndex,
                            int sourceFaceIndex,
                            float[][] source);
}
