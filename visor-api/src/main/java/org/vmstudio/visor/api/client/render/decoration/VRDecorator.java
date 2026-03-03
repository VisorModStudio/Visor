package org.vmstudio.visor.api.client.render.decoration;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import lombok.Setter;
import org.vmstudio.visor.api.common.addon.component.ComponentPriority;
import org.vmstudio.visor.api.common.addon.component.PrioritySupporter;
import org.vmstudio.visor.api.common.addon.VisorAddon;
import org.vmstudio.visor.api.common.addon.component.VisorComponent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class VRDecorator implements VisorComponent, PrioritySupporter {
    @Getter
    private final VisorAddon owner;
    @Getter
    private final String id;


    @Getter @Setter
    private boolean enabled = true;


    public VRDecorator(@NotNull VisorAddon owner,
                       @NotNull String id){
        this.owner = owner;
        this.id = id.toLowerCase();
    }

    public void init(){

    }

    public void clear(){

    }


    public abstract void tick();


    /**
     * If supports world hands
     *
     * <p>Override to return {@code true} for decorators
     * where the player is in-world
     *
     * @return true/false
     */
    public boolean supportsWorldHands() {
        return false;
    }

    /**
     * If this decorator fully controls the VR decoration rendering.
     *
     * <p>
     *     When true, no hands, overlays etc will be rendered in {@link VRDecorationRenderer},
     *     you will have full control.
     * </p>
     * @return true/false
     */
    public boolean isFullControl(){
        return false;
    }


    // ==========================================
    //  Staged render methods — override as needed
    // ==========================================

    /**
     * Unlike other methods, called once per game frame
     * <p>
     * Since in VR we render game multiple times,
     * it is important in some cases to update
     * data states one time right before the frame rendering
     *
     */
    public void updateRenderState() {
    }

    /**
     * Called before any pipeline stages, at the very beginning
     * of the frame for this decorator.
     *
     * <p>Use for setup work like in-block effects, panorama
     * rendering, light layer toggling, etc.</p>
     *
     * @param poseStack    the current pose stack
     * @param partialTicks partial tick time
     */
    public void setupRendering(@NotNull PoseStack poseStack, float partialTicks) {
    }

    /**
     * Called at {@code AFTER_SOLID} — before translucents.
     *
     * <p>Render depth-tested elements that should be visible
     * through water and translucent blocks
     * (e.g. depth overlays, VR hands).</p>
     *
     * <p>The default implementation does nothing.
     * The orchestrator ({@code DecorationRendererImpl}) handles
     * the standard pipeline (depth overlays + hands) automatically.
     * Override only to add custom decorator-specific rendering
     * at this stage.</p>
     *
     * @param poseStack    the current pose stack
     * @param partialTicks partial tick time
     */
    public void renderAfterSolid(@NotNull PoseStack poseStack, float partialTicks) {
    }

    /**
     * Called at {@code AFTER_TRANSLUCENT} — after translucent pass.
     *
     * <p>Render elements that should properly sort with
     * translucent world geometry.</p>
     *
     * <p>The default implementation does nothing.</p>
     *
     * @param poseStack    the current pose stack
     * @param partialTicks partial tick time
     */
    public void renderAfterTranslucent(@NotNull PoseStack poseStack, float partialTicks) {
    }

    /**
     * Called at {@code AFTER_WORLD} — the final 3D stage.
     *
     * <p>Render elements that go on top of the entire 3D scene
     * (e.g. game effects, HUD overlays, cursor).</p>
     *
     * <p>The default implementation does nothing.
     * The orchestrator handles the standard pipeline
     * (game effects + HUD overlays + cursor) automatically.
     * Override only for custom decorator-specific rendering.</p>
     *
     * @param poseStack    the current pose stack
     * @param partialTicks partial tick time
     */
    public void renderAfterWorld(@NotNull PoseStack poseStack, float partialTicks) {
    }





    public abstract boolean canActivate();

    /**
     * Supported game effects(non-global)
     *
     * @return list of non-global game effect ids
     */
    public abstract List<String> gameEffects();

    /**
     * Supported hand effects(non-global)
     *
     * @return list of non-global hand effect ids
     */
    public abstract List<String> handEffects();


    public boolean isEnabledAndCanActivate() {
        return enabled && canActivate();
    }

    @Override
    public @NotNull ComponentPriority getPriority() {
        return ComponentPriority.NORMAL;
    }
}