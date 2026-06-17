package org.vmstudio.visor.compatibility.iris.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.buffer.ShaderStorageBufferHolder;
import net.irisshaders.iris.gl.image.GlImage;
import net.irisshaders.iris.pipeline.IrisRenderingPipeline;
import net.irisshaders.iris.shadows.ShadowRenderTargets;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vmstudio.visor.api.client.render.VRRenderPass;
import org.vmstudio.visor.compatibility.ClassDependentMixin;
import org.vmstudio.visor.compatibility.iris.IrisCompatHelper;
import org.vmstudio.visor.compatibility.iris.extensions.IrisPipelineExtension;
import org.vmstudio.visor.compatibility.iris.extensions.IrisPipelineManagerExtension;
import org.vmstudio.visor.core.client.VisorClientImpl;

import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

@Pseudo
@ClassDependentMixin("net.irisshaders.iris.pipeline.IrisRenderingPipeline")
@Mixin(value = IrisRenderingPipeline.class, remap = false)
public class IrisRenderingPipelineVRMixin implements IrisPipelineExtension {
    @Shadow
    @Final
    private Set<GlImage> customImages;

    @Shadow
    private ShaderStorageBufferHolder shaderStorageBufferHolder;

    @Shadow
    private ShadowRenderTargets shadowRenderTargets;

    @Final
    @Mutable
    @Shadow
    private Supplier<ShadowRenderTargets> shadowTargetsSupplier;

    @Unique
    private boolean visor$shadowSharer;

    @Unique
    private boolean visor$sharedSsbo;

    @Redirect(method = "<init>",
            at = @At(value = "NEW",
                    target = "(Lit/unimi/dsi/fastutil/ints/Int2ObjectArrayMap;II)Lnet/irisshaders/iris/gl/buffer/ShaderStorageBufferHolder;"),
            require = 0)
    private ShaderStorageBufferHolder visor$shareSsboHolder(Int2ObjectArrayMap<?> bufferInfos, int width, int height) {
        if (IrisCompatHelper.shareSsbo()
                && IrisCompatHelper.perEyePipelines()
                && IrisCompatHelper.buildingPass != null
                && Iris.getPipelineManager() instanceof IrisPipelineManagerExtension manager
                && manager.visor$getBasePipeline() instanceof IrisPipelineExtension basePipeline
                && basePipeline.visor$getSsboHolder() instanceof ShaderStorageBufferHolder sharedHolder) {
            visor$sharedSsbo = true;
            return sharedHolder;
        }
        return new ShaderStorageBufferHolder((Int2ObjectArrayMap) bufferInfos, width, height);
    }

    @WrapOperation(method = "destroy",
            at = @At(value = "INVOKE",
                    target = "Lnet/irisshaders/iris/gl/buffer/ShaderStorageBufferHolder;destroyBuffers()V"),
            require = 0)
    private void visor$keepSharedSsbo(ShaderStorageBufferHolder holder, Operation<Void> original) {
        if (!visor$sharedSsbo) {
            original.call(holder);
        }
    }

    @WrapOperation(method = "<init>", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
            target = "Lnet/irisshaders/iris/pipeline/IrisRenderingPipeline;shadowTargetsSupplier:Ljava/util/function/Supplier;",
            ordinal = 0))
    private void visor$shareShadowTargets(IrisRenderingPipeline instance, Supplier<ShadowRenderTargets> value,
                                          Operation<Void> original) {
        visor$shadowSharer = IrisCompatHelper.shareShadows()
                && IrisCompatHelper.perEyePipelines()
                && !IrisCompatHelper.slowMode
                && IrisCompatHelper.buildingPass != null
                && IrisCompatHelper.buildingPass != VRRenderPass.EYE_LEFT;
        Supplier<ShadowRenderTargets> wrapped = () -> {
            Object shared = IrisCompatHelper.sharedShadowTargets;
            if (visor$shadowSharer && this.shadowRenderTargets == null && shared != null) {
                return (ShadowRenderTargets) shared;
            }
            return value.get();
        };
        original.call(instance, wrapped);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void visor$classifyPipeline(CallbackInfo ci) {
        IrisCompatHelper.slowMode =
                (this.customImages != null && !this.customImages.isEmpty())
                        || (this.shaderStorageBufferHolder != null && !IrisCompatHelper.shareSsbo());
        if (IrisCompatHelper.buildingPass == null) {
            VisorClientImpl.LOGGER.info(
                    "Visor: shader pack classified {} for VR (custom images: {}, SSBOs: {}, SSBO sharing: {})",
                    IrisCompatHelper.slowMode ? "SLOW-MODE (fully isolated per-pass state)" : "fast-mode",
                    this.customImages == null ? 0 : this.customImages.size(),
                    this.shaderStorageBufferHolder != null,
                    IrisCompatHelper.shareSsbo());
        }

        if (IrisCompatHelper.shareShadows()
                && IrisCompatHelper.perEyePipelines()
                && !IrisCompatHelper.slowMode
                && IrisCompatHelper.buildingPass == VRRenderPass.EYE_LEFT
                && IrisCompatHelper.sharedShadowTargets == null) {
            IrisCompatHelper.sharedShadowTargets = this.shadowRenderTargets;
        }
    }

    @ModifyArg(method = "lambda$new$*",
            at = @At(value = "INVOKE",
                    target = "Ljava/util/Objects;requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;"),
            expect = 0, require = 0)
    private Object visor$rerouteSharedShadowTargets(Object obj) {
        if (visor$shadowSharer && (obj == null || obj instanceof ShadowRenderTargets)) {
            return Objects.requireNonNullElse(IrisCompatHelper.sharedShadowTargets, obj);
        }
        return obj;
    }

    @ModifyReturnValue(method = "shouldDisableVanillaEntityShadows", at = @At("RETURN"))
    private boolean visor$matchOwnerEntityShadows(boolean original) {
        return original || (visor$shadowSharer && IrisCompatHelper.sharedShadowActive());
    }

    @Override
    @Unique
    public Object visor$getSsboHolder() {
        return this.shaderStorageBufferHolder;
    }

    @Override
    @Unique
    public boolean visor$isShadowSharer() {
        return visor$shadowSharer;
    }
}
