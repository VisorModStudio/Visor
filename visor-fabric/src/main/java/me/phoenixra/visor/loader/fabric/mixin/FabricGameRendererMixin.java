package me.phoenixra.visor.loader.fabric.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import me.phoenixra.visor.core.client.render.helpers.RenderHelper;
import me.phoenixra.visor.core.client.render.VRRenderState;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(GameRenderer.class)
public class FabricGameRendererMixin {
    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V ", ordinal = 2), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void removeMulPoseXRotation(PoseStack s, Quaternionf quaternion) {
        if (VRRenderState.getCurrentPhase().isVanilla()) {
            s.mulPose(quaternion);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V ", ordinal = 3), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void removeMulPoseYRotation(PoseStack s, Quaternionf quaternion) {
        if (VRRenderState.getCurrentPhase().isVanilla()) {
            s.mulPose(quaternion);
        } else {
            RenderHelper.applyDisplayOrientation(VRRenderState.getCurrentVRDisplay(), s);
        }
    }

}
