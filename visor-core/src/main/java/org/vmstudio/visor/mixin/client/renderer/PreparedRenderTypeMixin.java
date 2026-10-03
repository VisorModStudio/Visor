// #!MC-VERSION:: 26.3+
package org.vmstudio.visor.mixin.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import com.mojang.renderpearl.api.commands.RenderPass;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.vmstudio.visor.api.compatibility.mcversion.render.McShaderProgram;

@Mixin(PreparedRenderType.class)
public abstract class PreparedRenderTypeMixin {

    @Shadow @Final
    private RenderPipeline pipeline;

    @WrapOperation(method = "draw",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;bindDefaultUniforms(Lcom/mojang/renderpearl/api/commands/RenderPass;)V"),
            require = 1)
    private void visor$bindProgramUniforms(RenderPass pass, Operation<Void> original) {
        original.call(pass);
        McShaderProgram.prepareRenderTypeDraw(this.pipeline);
        McShaderProgram.bindRenderTypeDraw(pass, this.pipeline);
    }
}
