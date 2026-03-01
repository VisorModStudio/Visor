package org.vmstudio.visor.mixin.client.audio;

import com.mojang.blaze3d.audio.Library;
import org.vmstudio.visor.core.client.VisorState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Library.class)
public class LibraryMixin {
    /**
     * Better sound for VR
     * @param defaultHRTF s
     * @return s
     */
    @ModifyVariable(method = "init", at = @At("HEAD"), argsOnly = true)
    private boolean visor$enableHRTF(boolean defaultHRTF) {
        if (VisorState.get().isActive()) {
            return true;
        }
        return defaultHRTF;
    }
}
