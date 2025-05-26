package me.phoenixra.visor.api.client.render.context;

import me.phoenixra.atumvr.api.rendering.IRenderContext;
import net.minecraft.util.profiling.ProfilerFiller;

public record PreRenderContext(ProfilerFiller profilerFiller,
                               boolean gameIsTicking,
                               float partialTick) implements IRenderContext {
}
