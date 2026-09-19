// #!MC-VERSION:: 1.20.1-1.21.5
package org.vmstudio.visor.mixin.client.renderer;

import net.minecraft.client.renderer.ScreenEffectRenderer;
import org.spongepowered.asm.mixin.Mixin;

// [EMPTY SHELL before 1.21.6] the item activation animation lives in GameRendererMixin there
@Mixin(ScreenEffectRenderer.class)
public abstract class ScreenEffectRendererMixin {
}
