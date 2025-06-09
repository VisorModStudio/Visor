package me.phoenixra.visor.core.mixin.client.audio;

import com.mojang.blaze3d.audio.Library;
import me.phoenixra.visor.core.client.VisorState;
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
        if (VisorState.getState().isActive()) {
            return true;
        }
        return defaultHRTF;
    }
}
