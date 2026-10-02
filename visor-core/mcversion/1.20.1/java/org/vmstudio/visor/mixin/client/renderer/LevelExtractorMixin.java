// #!MC-VERSION:: 1.20.1-26.1.2
package org.vmstudio.visor.mixin.client.renderer;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;

// [EMPTY SHELL before 26.2] LevelRenderer extracts the level itself, see LevelRendererMixin
@Mixin(LevelRenderer.class)
public abstract class LevelExtractorMixin {
}
