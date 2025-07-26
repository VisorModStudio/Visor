package me.phoenixra.visor.core.client.render.context;

import me.phoenixra.atumvr.api.rendering.IRenderContext;
import net.minecraft.util.profiling.ProfilerFiller;

public record PreRenderContext(ProfilerFiller profiler,
                               boolean gameIsTicking,
                               float partialTicks) implements IRenderContext {
}
