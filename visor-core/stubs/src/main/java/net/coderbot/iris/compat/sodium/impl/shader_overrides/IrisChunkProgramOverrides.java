package net.coderbot.iris.compat.sodium.impl.shader_overrides;

import me.jellysquid.mods.sodium.client.gl.shader.GlProgram;
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexType;
import net.coderbot.iris.pipeline.SodiumTerrainPipeline;

import java.util.EnumMap;

public class IrisChunkProgramOverrides {
    @SuppressWarnings("unused")
    private final EnumMap<IrisTerrainPass, GlProgram<IrisChunkShaderInterface>> programs = null;

    public void createShaders(SodiumTerrainPipeline pipeline, ChunkVertexType vertexType) {
        throw new AssertionError("compile-only stub");
    }
}