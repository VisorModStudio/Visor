// #!MC-VERSION:: 26.2
package org.vmstudio.visor.mixin.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.renderer.rendertype.PreparedRenderType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.compatibility.mcversion.render.McShaderProgram;

@Mixin(PreparedRenderType.class)
public abstract class PreparedRenderTypeMixin {

    @Shadow @Final
    private RenderPipeline pipeline;

    @Inject(method = "drawFromBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/IndexType;III)V",
            at = @At("HEAD"))
    private void visor$writeProgramUniforms(CallbackInfo ci) {
        McShaderProgram.prepareRenderTypeDraw(this.pipeline);
    }

    @WrapOperation(method = "drawFromBuffer(Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/buffers/GpuBuffer;Lcom/mojang/blaze3d/IndexType;III)V",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;bindDefaultUniforms(Lcom/mojang/blaze3d/systems/RenderPass;)V"),
            require = 1)
    private void visor$bindProgramUniforms(RenderPass pass, Operation<Void> original) {
        original.call(pass);
        McShaderProgram.bindRenderTypeDraw(pass, this.pipeline);
    }
}
