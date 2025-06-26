package me.phoenixra.visor.api.client.gui.overlay;

import com.mojang.blaze3d.pipeline.RenderTarget;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.client.gui.overlay.template.OverlayTemplate;
import me.phoenixra.visor.api.common.addon.element.PrioritySupporter;
import me.phoenixra.visor.api.common.addon.element.VisorElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Basic interface for all VR overlays.
 *
 * <p>Implements {@link PrioritySupporter} to control the draw order</p>
 */
public interface VROverlay extends VisorElement, PrioritySupporter {


    /**
     * Tick overlay
     */
    void tick();

    /**
     * Update overlay pose
     *
     * @param partialTicks the partial ticks
     */
    void updatePose(float partialTicks);


    /**
     * Get overlay pose.
     *
     * @return the overlay pose
     */
    @NotNull
    VROverlayPose getPose();


    /**
     * Get overlay render target
     *
     * @return the render target
     */
    @Nullable
    RenderTarget getRenderTarget();


    /**
     * If overlay is within view distance
     *
     * <p>This method is called every tick when overlay is enabled,
     * to check if overlay can be visible</p>
     *
     * @return true/false
     */
    default boolean isInViewDistance(){
        var hmdPos = VisorAPI.client().getPlayer()
                .getPose(PoseDataType.PRE_TICK)
                .getHmd().getPosition();
        return hmdPos.distance(getPose().getPosition()) < 5;
    }

    /**
     * If overlay is visible
     *
     * @return true/false
     */
    boolean isVisible();


    /**
     * Get this object as overlay type
     *
     * @return overlay type or null if not an instance of {@link OverlayTemplate}
     */
    default @Nullable OverlayTemplate asTemplate(){
        if(this instanceof OverlayTemplate overlayTemplate){
            return overlayTemplate;
        }else{
            return null;
        }
    }


    /**
     * If overlay visibility is affected
     * by solid blocks, entity models etc
     *
     * <p>When false, overlay is displayed on a layer above world</p>
     *
     * @return true/false
     */
    default boolean supportsDepth(){
        return false;
    }

    /**
     * If cursor handling is supported
     *
     * @return true/false
     */
    default boolean supportsCursor() {
        return true;
    }

    /**
     * If two cursors are supported
     *
     * @return true/false
     */
    default boolean supportsTwoCursors(){
        return false;
    }



    /**
     * Get Data for active cursor
     *
     * @return the cursor data
     */
    @NotNull
    VROverlayCursorData getActiveCursorData();

    /**
     * Get Data for inactive cursor
     *
     * <p>Useful if {@link #supportsTwoCursors()} = true</p>
     *
     * @return the cursor data
     */
    @NotNull
    VROverlayCursorData getInactiveCursorData();


    /**
     * Update cursor data
     *
     * @param activeCursor if active or inactive cursor
     * @param rawX the raw cursor X position relative to overlay bounds
     * @param rawY the raw cursor Y position relative to overlay bounds
     */
    void updateCursorData(boolean activeCursor,
                          float rawX, float rawY);

    /**
     * If specified cursor raw data is within overlay bounds
     *
     * @param activeCursor if active or inactive cursor
     * @param rawX the raw cursor X position relative to overlay bounds
     * @param rawY the raw cursor Y position relative to overlay bounds
     *
     * @return true/false
     */
    default boolean isCursorWithinBounds(boolean activeCursor,
                                         float rawX, float rawY) {
        return rawX > 0f
                && rawX < 1f
                && rawY > 0f
                && rawY < 1f;
    }


    /**
     * Get active cursor position X
     *
     * <p>Shortcut of {@link VROverlayCursorData#getCursorX()} for {@link #getActiveCursorData()}</p>
     *
     * <p>Here we use 'Mouse' naming,
     * since we are emulating minecraft screen behaviour for mouse</p>
     *
     * @return int from 0 to overlay width
     */
    default int getMouseX(){
        return getActiveCursorData().getCursorX();
    }

    /**
     * Get active cursor position Y.
     *
     * <p>Shortcut of {@link VROverlayCursorData#getCursorX()} for {@link #getActiveCursorData()}</p>
     *
     * <p>Here we use 'Mouse' naming,
     * since we are emulating minecraft screen behaviour for mouse</p>
     *
     * @return int from 0 to overlay height
     */
    default int getMouseY(){
        return getActiveCursorData().getCursorY();
    }

    /**
     * Get active cursor raw position X.
     *
     * <p>Shortcut of {@link VROverlayCursorData#getCursorX()} for {@link #getActiveCursorData()}</p>
     *
     * <p>Here we use 'Mouse' naming,
     * since we are emulating minecraft screen behaviour for mouse</p>
     *
     * @return float from 0 to 1
     */
    default float getRawMouseX(){
        return getActiveCursorData().getRawCursorX();
    }

    /**
     * Get active cursor raw position Y.
     *
     * <p>Shortcut of {@link VROverlayCursorData#getCursorX()} for {@link #getActiveCursorData()}</p>
     *
     * <p>Here we use 'Mouse' naming,
     * since we are emulating minecraft screen behaviour for mouse</p>
     *
     * @return float from 0 to 1
     */
    default float getRawMouseY(){
        return getActiveCursorData().getRawCursorY();
    }


    /**
     * On Mouse clicked
     *
     * <p>Emulates the same method in minecraft screen</p>
     *
     * @param x the mouse position X
     * @param y the mouse position Y
     * @param buttonType the button type (0 - left, 1 - right, 3 - scroll)
     *
     * @return if succeeded
     */
    boolean mouseClicked(double x, double y, int buttonType);

    /**
     * On Mouse released
     *
     * <p>Emulates the same method in minecraft screen</p>
     *
     * @param mouseX the mouse position X
     * @param mouseY the mouse position Y
     * @param buttonType the button type (0 - left, 1 - right, 3 - scroll)
     *
     * @return if succeeded
     */
    boolean mouseReleased(double mouseX, double mouseY, int buttonType);

    /**
     * On Mouse scrolled
     *
     * <p>Emulates the same method in minecraft screen</p>
     *
     * @param mouseX the mouse position X
     * @param mouseY the mouse position Y
     * @param scrollDelta the scroll delta
     *
     * @return if succeeded
     */
    boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta);

    /**
     * On Mouse moved
     *
     * <p>Emulates the same method in minecraft screen</p>
     *
     * @param mouseX the mouse position X
     * @param mouseY the mouse position Y
     */
    void mouseMoved(double mouseX, double mouseY);

    /**
     * On Mouse dragged
     *
     * <p>Emulates the same method in minecraft screen</p>
     *
     * @param mouseX the mouse position X
     * @param mouseY the mouse position Y
     * @param buttonType the button type (0 - left, 1 - right, 3 - scroll)
     * @param deltaX the delta between current and previous mouse X position
     * @param deltaY the delta between current and previous mouse Y position
     *
     * @return if succeeded
     */
    boolean mouseDragged(double mouseX, double mouseY,
                         int buttonType,
                         double deltaX, double deltaY);


    /**
     * On Key pressed
     *
     * <p>Emulates the same method in minecraft screen</p>
     *
     * @param keyCode the key code
     * @param keyScan the key scan
     * @param modifiers the modifiers
     *
     * @return if succeeded
     */
    boolean keyPressed(int keyCode, int keyScan, int modifiers);

    /**
     * On Key released
     *
     * <p>Emulates the same method in minecraft screen</p>
     *
     * @param keyCode the key code
     * @param keyScan the key scan
     * @param modifiers the modifiers
     *
     * @return if succeeded
     */
    boolean keyReleased(int keyCode, int keyScan, int modifiers);

    /**
     * On Char typed
     *
     * <p>Emulates the same method in minecraft screen</p>
     *
     * @param chr the char
     * @param modifiers the modifiers
     *
     * @return if succeeded
     */
    boolean charTyped(char chr, int modifiers);


    /**
     * Override of {@link PrioritySupporter#compareTo(PrioritySupporter)}
     * to sort elements in reverse priority order,
     * since we need top priority to be rendered the last
     *
     * @param o the object to be compared.
     * @return result
     */
    @Override
    default int compareTo(@NotNull PrioritySupporter o) {
        return Integer.compare(
                -o.getPriority().getWeight(),
                -getPriority().getWeight()
        );
    }

}
