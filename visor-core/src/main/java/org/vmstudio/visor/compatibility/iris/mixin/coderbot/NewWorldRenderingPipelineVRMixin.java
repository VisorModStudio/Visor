package org.vmstudio.visor.compatibility.iris.mixin.coderbot;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline;
import net.coderbot.iris.shadows.ShadowRenderTargets;
import org.objectweb.asm.Opcodes;
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
import org.vmstudio.visor.compatibility.MixinGate;
import org.vmstudio.visor.compatibility.iris.IrisCompatHelper;
import org.vmstudio.visor.compatibility.iris.extensions.IrisPipelineExtension;
import org.vmstudio.visor.core.client.VisorClientImpl;

import java.lang.reflect.Field;
import java.util.Set;
import java.util.function.Supplier;

@Pseudo
@MixinGate(classes = "net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline")
@Mixin(value = NewWorldRenderingPipeline.class, remap = false)
public class NewWorldRenderingPipelineVRMixin implements IrisPipelineExtension {
    @Shadow
    private ShadowRenderTargets shadowRenderTargets;

    @Final
    @Mutable
    @Shadow
    private Supplier<ShadowRenderTargets> shadowTargetsSupplier;

    @Unique
    private boolean visor$shadowSharer;

    @Unique
    private Object visor$ssboHolder;

    @WrapOperation(method = "<init>", at = @At(value = "FIELD", opcode = Opcodes.PUTFIELD,
            target = "Lnet/coderbot/iris/pipeline/newshader/NewWorldRenderingPipeline;shadowTargetsSupplier:Ljava/util/function/Supplier;",
            ordinal = 0))
    private void visor$shareShadowTargets(NewWorldRenderingPipeline instance, Supplier<ShadowRenderTargets> value,
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
        boolean slow = false;
        visor$ssboHolder = null;
        try {
            Class<?> pipelineClass =
                    Class.forName("net.coderbot.iris.pipeline.newshader.NewWorldRenderingPipeline");
            Field customImages = pipelineClass.getDeclaredField("customImages");
            customImages.setAccessible(true);
            Field ssboHolder = pipelineClass.getDeclaredField("shaderStorageBufferHolder");
            ssboHolder.setAccessible(true);
            visor$ssboHolder = ssboHolder.get(this);
            slow = (customImages.get(this) instanceof Set<?> images && !images.isEmpty())
                    || visor$ssboHolder != null;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException ignored) {}
        IrisCompatHelper.slowMode = slow;
        if (IrisCompatHelper.buildingPass == null) {
            VisorClientImpl.LOGGER.info(
                    "Visor: shader pack classified {} for VR (legacy Iris; SSBOs: {})",
                    slow ? "SLOW-MODE (fully isolated per-pass state)" : "fast-mode",
                    visor$ssboHolder != null);
        }
        if (IrisCompatHelper.shareShadows()
                && IrisCompatHelper.perEyePipelines()
                && !IrisCompatHelper.slowMode
                && IrisCompatHelper.buildingPass == VRRenderPass.EYE_LEFT
                && IrisCompatHelper.sharedShadowTargets == null) {
            IrisCompatHelper.sharedShadowTargets = this.shadowRenderTargets;
        }
    }


    @ModifyArg(method = {"addGbufferOrShadowSamplers*", "lambda$new$*"},
            at = @At(value = "INVOKE",
                    target = "Ljava/util/Objects;requireNonNull(Ljava/lang/Object;)Ljava/lang/Object;"),
            expect = 0, require = 0)
    private Object visor$rerouteSharedShadowTargets(Object obj) {
        if (!visor$shadowSharer || (obj != null && !(obj instanceof ShadowRenderTargets))) {
            return obj;
        }
        Object shared = IrisCompatHelper.sharedShadowTargets;
        return shared != null ? shared : obj;
    }

    @ModifyReturnValue(method = "shouldDisableVanillaEntityShadows", at = @At("RETURN"))
    private boolean visor$matchOwnerEntityShadows(boolean original) {
        return original || (visor$shadowSharer && IrisCompatHelper.sharedShadowActive());
    }

    @Override
    @Unique
    public Object visor$getSsboHolder() {
        return visor$ssboHolder;
    }

    @Override
    @Unique
    public boolean visor$isShadowSharer() {
        return visor$shadowSharer;
    }
}