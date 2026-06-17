package org.vmstudio.visor.compatibility.iris.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vmstudio.visor.compatibility.ClassDependentMixin;
import org.vmstudio.visor.core.client.VisorClientImpl;

@Pseudo
@ClassDependentMixin("net.irisshaders.iris.uniforms.custom.CustomUniforms")
@Mixin(targets = "net.irisshaders.iris.uniforms.custom.CustomUniforms", remap = false)
public class CustomUniformsMixin {
    @Redirect(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/irisshaders/iris/IrisLogging;warn(Ljava/lang/String;Ljava/lang/Throwable;)V"
            ),
            remap = false
    )
    private void visor$ignoreMissingUniformField(@Coerce Object logger, String warning, Throwable throwable) {
        if (visor$isMissingFieldWarning(warning, throwable)) {
            VisorClientImpl.LOGGER.debug("[Iris] suppressed custom uniform error: {}", warning);
            return;
        }

        VisorClientImpl.LOGGER.warn("[Iris] {}", warning, throwable);
    }

    @Unique
    private boolean visor$isMissingFieldWarning(String warning, Throwable throwable) {
        String message = warning != null ? warning : "";
        String causeMessage = throwable != null && throwable.getMessage() != null ? throwable.getMessage() : "";

        return message.contains("Unknown variable")
                || causeMessage.contains("Unknown variable")
                || message.contains("NoSuchField")
                || causeMessage.contains("NoSuchField")
                || throwable instanceof NoSuchFieldException
                || throwable instanceof NoSuchFieldError;
    }
}
