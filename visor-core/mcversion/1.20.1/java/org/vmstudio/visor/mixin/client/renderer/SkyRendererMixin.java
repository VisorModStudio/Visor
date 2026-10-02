// #!MC-VERSION:: 1.20.1-26.1.2
package org.vmstudio.visor.mixin.client.renderer;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;

// [EMPTY SHELL before 26.2] the sky draws to the main render target of the moment
@Mixin(LevelRenderer.class)
public abstract class SkyRendererMixin {
}
