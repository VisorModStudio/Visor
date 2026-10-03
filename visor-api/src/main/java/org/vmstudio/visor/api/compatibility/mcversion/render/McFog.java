package org.vmstudio.visor.api.compatibility.mcversion.render;

import com.mojang.blaze3d.systems.RenderSystem;
//? if >=1.21.6 {
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.fog.FogRenderer;
//?} elif >=1.21.2 {
/*import net.minecraft.client.renderer.FogParameters;
*///?}

/**
 * Cross-mc-version facade over the shader fog
 */
public class McFog {
    private McFog() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    public static State save() {
        //? if >=1.21.2 {
        return new State(RenderSystem.getShaderFog());
        //?} else {
        /*return new State(RenderSystem.getShaderFogStart());
        *///?}
    }

    public static void disable() {
        //? if >=1.21.6 {
        RenderSystem.setShaderFog(Minecraft.getInstance().gameRenderer.fogRenderer.getBuffer(FogRenderer.FogMode.NONE));
        //?} elif >=1.21.2 {
        /*RenderSystem.setShaderFog(FogParameters.NO_FOG);
        *///?} else {
        /*RenderSystem.setShaderFogStart(Float.MAX_VALUE);
        *///?}
    }

    public static void restore(State state) {
        //? if >=1.21.2 {
        RenderSystem.setShaderFog(state.fog);
        //?} else {
        /*RenderSystem.setShaderFogStart(state.fogStart);
        *///?}
    }

    public static final class State {
        //? if >=1.21.6 {
        private final GpuBufferSlice fog;

        private State(GpuBufferSlice fog) {
            this.fog = fog;
        }
        //?} elif >=1.21.2 {
        /*private final FogParameters fog;

        private State(FogParameters fog) {
            this.fog = fog;
        }
        *///?} else {
        /*private final float fogStart;

        private State(float fogStart) {
            this.fogStart = fogStart;
        }
        *///?}
    }
}
