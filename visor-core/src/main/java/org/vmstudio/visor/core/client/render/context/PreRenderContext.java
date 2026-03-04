package org.vmstudio.visor.core.client.render.context;


import me.phoenixra.atumvr.api.rendering.AtumVRRenderContext;
import net.minecraft.util.profiling.ProfilerFiller;

public record PreRenderContext(ProfilerFiller profiler,
                               boolean gameIsTicking,
                               float partialTicks) implements AtumVRRenderContext {
}
