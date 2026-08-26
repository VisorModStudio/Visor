package org.vmstudio.visor.compatibility.sodium.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.vmstudio.visor.compatibility.sodium.FaceUv;
import org.vmstudio.visor.compatibility.sodium.SodiumFaces;
import org.vmstudio.visor.compatibility.sodium.extensions.ModelCuboidExtension;

@Pseudo
@Mixin(targets = {
        "me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid",
        "net.caffeinemc.mods.sodium.client.render.immediate.model.ModelCuboid"
})
public class ModelCuboidMixin implements ModelCuboidExtension {
    @Unique
    private FaceUv[] visor$faceOverrides = null;

    @Override
    public FaceUv[] visor$faceOverrides() {
        return this.visor$faceOverrides;
    }

    @Override
    public void visor$overrideFace(int sodiumFace, FaceUv uv) {
        if (this.visor$faceOverrides == null) {
            this.visor$faceOverrides = new FaceUv[SodiumFaces.COUNT];
        }
        this.visor$faceOverrides[sodiumFace] = uv;
    }
}
