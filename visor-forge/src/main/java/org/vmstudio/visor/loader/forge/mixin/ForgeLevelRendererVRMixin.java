package org.vmstudio.visor.loader.forge.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.client.render.RenderPipelineStage;
import org.vmstudio.visor.loader.forge.ForgeModLoader;

/**
 * Drives Visor's render pipeline stages on Forge.
 * <p>
 * 1.21.4: Forge 54 dropped {@code RenderLevelStageEvent} when level rendering moved to the
 * frame graph, and the only hook left ({@code AddFramePassEvent}) appends a pass after every
 * vanilla one, so it cannot express AFTER_SOLID or AFTER_TRANSLUCENT. The stages are fired
 * from here instead, at the same points the old event used:
 * <ul>
 *     <li>AFTER_SOLID after the cutout terrain layer (before entities), matching the old
 *         {@code AFTER_CUTOUT_BLOCKS}</li>
 *     <li>AFTER_TRANSLUCENT after the translucent terrain layer, matching the old
 *         {@code AFTER_TRANSLUCENT_BLOCKS}</li>
 *     <li>AFTER_WORLD once the frame graph has executed, matching the old
 *         {@code AFTER_LEVEL}</li>
 * </ul>
 * The callbacks get a fresh {@link PoseStack} - the decoration renderers build their own
 * camera transform, the same contract the Fabric implementation relies on.
 */
@Mixin(LevelRenderer.class)
public class ForgeLevelRendererVRMixin {

    @Inject(method = "renderSectionLayer", at = @At("TAIL"))
    private void visor$afterSectionLayer(RenderType renderType, double x, double y, double z,
                                         org.joml.Matrix4f frustumMatrix,
                                         org.joml.Matrix4f projectionMatrix,
                                         CallbackInfo ci) {
        RenderPipelineStage stage;
        if (renderType == RenderType.cutout()) {
            stage = RenderPipelineStage.AFTER_SOLID;
        } else if (renderType == RenderType.translucent()) {
            stage = RenderPipelineStage.AFTER_TRANSLUCENT;
        } else {
            return;
        }
        ForgeModLoader.fireRenderPipelineStage(stage, new PoseStack(), visor$partialTicks());
    }

    @Inject(method = "renderLevel", at = @At("TAIL"))
    private void visor$afterLevel(CallbackInfo ci) {
        ForgeModLoader.fireRenderPipelineStage(
                RenderPipelineStage.AFTER_WORLD, new PoseStack(), visor$partialTicks());
    }

    private static float visor$partialTicks() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(true);
    }
}
