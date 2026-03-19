package org.vmstudio.visor.compatibility.sodium.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.vmstudio.visor.compatibility.sodium.extensions.ModelCuboidExtension;

@Pseudo
@Mixin(targets = {
        "me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid",
        "net.caffeinemc.mods.sodium.client.render.immediate.model.ModelCuboid"
})
public class ModelCuboidMixin implements ModelCuboidExtension {
    @Unique
    private float[][] visor$overrides = null;

    @Override
    public float[][] visor$getOverrides() {
        return this.visor$overrides;
    }

    @Override
    public void visor$addOverrides(int overrideFaceIndex, int sourceFaceIndex, float[][] source) {
        if (this.visor$overrides == null) {
            this.visor$overrides = new float[6][5];
        }
        this.visor$overrides[overrideFaceIndex][0] = 1F;
        // order taken from me.jellysquid.mods.sodium.client.render.immediate.model.EntityRenderer.prepareVertices
        switch (sourceFaceIndex) {
            case 1 -> {
                this.visor$overrides[overrideFaceIndex][1] = source[0][2];
                this.visor$overrides[overrideFaceIndex][2] = source[1][1];
                this.visor$overrides[overrideFaceIndex][3] = source[0][3];
                this.visor$overrides[overrideFaceIndex][4] = source[1][0];
            }
            case 2 -> {
                this.visor$overrides[overrideFaceIndex][1] = source[0][1];
                this.visor$overrides[overrideFaceIndex][2] = source[1][1];
                this.visor$overrides[overrideFaceIndex][3] = source[0][2];
                this.visor$overrides[overrideFaceIndex][4] = source[1][2];
            }
            case 3 -> {
                this.visor$overrides[overrideFaceIndex][1] = source[0][4];
                this.visor$overrides[overrideFaceIndex][2] = source[1][1];
                this.visor$overrides[overrideFaceIndex][3] = source[0][5];
                this.visor$overrides[overrideFaceIndex][4] = source[1][2];
            }
            case 4 -> {
                this.visor$overrides[overrideFaceIndex][1] = source[0][2];
                this.visor$overrides[overrideFaceIndex][2] = source[1][1];
                this.visor$overrides[overrideFaceIndex][3] = source[0][4];
                this.visor$overrides[overrideFaceIndex][4] = source[1][2];
            }
            case 5 -> {
                this.visor$overrides[overrideFaceIndex][1] = source[0][0];
                this.visor$overrides[overrideFaceIndex][2] = source[1][1];
                this.visor$overrides[overrideFaceIndex][3] = source[0][1];
                this.visor$overrides[overrideFaceIndex][4] = source[1][2];
            }
            // 0 case
            default -> {
                this.visor$overrides[overrideFaceIndex][1] = source[0][1];
                this.visor$overrides[overrideFaceIndex][2] = source[1][0];
                this.visor$overrides[overrideFaceIndex][3] = source[0][2];
                this.visor$overrides[overrideFaceIndex][4] = source[1][1];
            }
        }
    }
}
