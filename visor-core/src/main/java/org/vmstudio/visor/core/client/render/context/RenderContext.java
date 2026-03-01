package org.vmstudio.visor.core.client.render.context;

import me.phoenixra.atumvr.api.rendering.VRRenderContext;
import net.minecraft.util.profiling.ProfilerFiller;

public record RenderContext(ProfilerFiller profiler,
                            boolean renderLevel,
                            long nanoTime,
                            float partialTicks) implements VRRenderContext {
}
