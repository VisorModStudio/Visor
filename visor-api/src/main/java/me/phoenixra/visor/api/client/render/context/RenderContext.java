package me.phoenixra.visor.api.client.render.context;

import me.phoenixra.atumvr.api.rendering.IRenderContext;
import net.minecraft.util.profiling.ProfilerFiller;

public record RenderContext(ProfilerFiller profilerFiller,
                            boolean renderLevel,
                            long nanoTime,
                            float partialTick) implements IRenderContext {
}
