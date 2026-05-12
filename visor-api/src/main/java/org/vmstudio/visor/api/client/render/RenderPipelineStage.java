package org.vmstudio.visor.api.client.render;


import org.vmstudio.visor.api.ModLoader;

/**
 * Defines the render pipeline stages used by Visor,
 * that are handled in {@link ModLoader mod loader}
 */
public enum RenderPipelineStage {

    /**
     * After world geometry, before translucent.
     * Participates in the fabulous pipeline.
     *
     * <p>Ideal for depth-tested elements that should be
     * visible through water/translucent blocks
     * (e.g. VR hands, depth overlays).</p>
     *
     */
    AFTER_SOLID,

    /**
     * After translucent pass — works with fabulous sorting.
     *
     * <p>Elements rendered here participate in
     * proper translucent sorting with water, stained-glass, etc.</p>
     *
     */
    AFTER_TRANSLUCENT,

    /**
     * After weather/particles, final 3D stage.
     * Equivalent to the end of renderLevel().
     *
     * <p>This is the primary stage for elements that
     * should render after everything in the 3D world
     * (e.g. game effects, HUD overlays, cursor).</p>
     *
     */
    AFTER_WORLD

}