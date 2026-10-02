// #!MC-VERSION:: 1.20.1-26.1.2
package org.vmstudio.visor.mixin.client.renderer;

import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;

// [EMPTY SHELL before 26.2] a render type draws itself, McShaderProgram overrides the draw of its own
@Mixin(LevelRenderer.class)
public abstract class PreparedRenderTypeMixin {
}
