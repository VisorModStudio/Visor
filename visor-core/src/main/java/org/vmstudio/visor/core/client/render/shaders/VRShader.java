package org.vmstudio.visor.core.client.render.shaders;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CompiledShaderProgram;
import net.minecraft.client.renderer.ShaderProgram;
import org.jetbrains.annotations.NotNull;

public interface VRShader {
    /**
     * The linked program. Only valid after {@link #init()}, and invalidated by a
     * resource reload - {@link org.vmstudio.visor.core.client.render.VRShaders#setup()}
     * must run again after one.
     */
    @NotNull
    CompiledShaderProgram getHandle();

    void init() throws Exception;


    @NotNull
    static CompiledShaderProgram link(@NotNull ShaderProgram program) throws Exception {
        return Minecraft.getInstance().getShaderManager().getProgramForLoading(program);
    }

}
