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
     * is compiled at the first lookup.
     * <p>
     * Deliberately {@code getProgramForLoading} rather than {@code getProgram}: on a compile
     * failure the latter runs Minecraft's resource-pack recovery - disabling the user's packs,
     * or calling {@code emergencySaveAndCrash} when there is nothing left to disable - and
     * caches the failure as {@code Optional.empty()}, so a later retry (renderer reinit,
     * resolution change) can never succeed until the next full resource reload. Pre-1.21.2
     * {@code new ShaderInstance(...)} simply threw, which is what Visor's own error handling
     * expects.
     */
    @NotNull
    static CompiledShaderProgram link(@NotNull ShaderProgram program) throws Exception {
        return Minecraft.getInstance().getShaderManager().getProgramForLoading(program);
    }

}
