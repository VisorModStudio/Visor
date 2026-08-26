package org.vmstudio.visor.compatibility.sodium.mixin.jellysquid;

import com.mojang.blaze3d.vertex.PoseStack;
import me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid;
import org.joml.Vector2f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.compatibility.sodium.FaceUv;
import org.vmstudio.visor.compatibility.sodium.extensions.ModelCuboidExtension;

@Pseudo
@Mixin(targets = "me.jellysquid.mods.sodium.client.render.immediate.model.EntityRenderer")
public class EntityRendererMixin {
    @Shadow(remap = false)
    private static void buildVertexTexCoord(Vector2f[] uvs, float u1, float v1, float u2, float v2) {}

    @Shadow(remap = false)
    @Final
    private static Vector2f[][] VERTEX_TEXTURES;

    @Inject(method = "prepareVertices", at = @At("TAIL"), remap = false)
    private static void visor$applyFaceUvOverrides(PoseStack.Pose matrices, ModelCuboid cuboid, CallbackInfo ci) {
        FaceUv[] overrides = ((ModelCuboidExtension) cuboid).visor$faceOverrides();
        if (overrides == null) return;
        for (int face = 0; face < overrides.length; face++) {
            FaceUv uv = overrides[face];
            if (uv != null) {
                buildVertexTexCoord(VERTEX_TEXTURES[face], uv.u1(), uv.v1(), uv.u2(), uv.v2());
            }
        }
    }
}
