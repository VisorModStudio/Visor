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

    /**
     * Links a shader program through the vanilla shader manager.
     * <p>
     * 1.21.2 replaced ShaderInstance with a {@link ShaderProgram} handle that the manager
     * compiles on demand. Programs are discovered by scanning {@code assets/<ns>/shaders/}
     * for {@code .json} files, so no registration step is needed; only the programs in
     * {@code CoreShaders.getProgramsToPreload()} are compiled up front, everything else
     * is compiled at the first lookup. The manager logs and returns null when compilation
     * fails, which this turns back into an exception so setup fails loudly.
     */
    @NotNull
    static CompiledShaderProgram link(@NotNull ShaderProgram program) throws Exception {
        CompiledShaderProgram compiled = Minecraft.getInstance().getShaderManager().getProgram(program);
        if (compiled == null) {
            throw new IllegalStateException(
                    "Failed to compile shader program '" + program.configId()
                            + "'; the shader manager logged the compile error"
            );
        }
        return compiled;
    }

}
