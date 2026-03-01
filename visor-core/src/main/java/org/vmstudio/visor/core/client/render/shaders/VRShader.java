package org.vmstudio.visor.core.client.render.shaders;

import net.minecraft.client.renderer.ShaderInstance;
import org.jetbrains.annotations.NotNull;

public interface VRShader {
    @NotNull
    ShaderInstance getHandle();

    void init() throws Exception;

}
