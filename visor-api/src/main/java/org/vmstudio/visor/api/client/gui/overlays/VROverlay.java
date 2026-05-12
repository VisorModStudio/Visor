package org.vmstudio.visor.api.client.gui.overlays;

import com.mojang.blaze3d.pipeline.RenderTarget;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.pose.PoseAnchor;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.client.gui.GuiTexture;
import org.vmstudio.visor.api.client.gui.overlays.options.OverlayOptionGroup;
import org.vmstudio.visor.api.common.addon.component.PrioritySupporter;
import org.vmstudio.visor.api.common.addon.component.VisorComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.Collection;

/**
 * Basic interface for all VR overlays.
 *
 * <p>Implements {@link PrioritySupporter} to control the draw order</p>
 */
public interface VROverlay extends VisorComponent, PrioritySupporter {


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
     * <p>If the overlay is not within view distance,
     * but {@link #isVisible()} won't be affected by that.</p>
     *
     * @return true/false
     */
    default boolean isInViewDistance(){
        var hmdPos = VisorAPI.client().getVRLocalPlayer()
                .getPoseData(PlayerPoseType.TICK)
                .getHmd().getPosition();
        return hmdPos.distance(getPose().getPosition()) < 5;
    }

    /**
     * If overlay is visible
     *
     * <p>Ignores {@link #isInViewDistance()}, so, if you want to know
     * when overlay is exactly visible, use these two methods together</p>
     *
     * @return true/false
     */
    boolean isVisible();


    /**
     * If this overlay is custom,
     * i.e. created from template
     *
     * @return true/false
     */
    default boolean isCustom(){
        return asTemplate() != null;
    }

    /**
     * If this overlay is built-in
     * i.e. created from code, no template used
     *
     * @return true/false
     */
    default boolean isBuiltIn(){
        return asTemplate() == null;
    }

    /**
     * Get this overlay as template
     *
     * @return overlay template or null if not an instance of {@link VROverlayTemplate}
     */
    default @Nullable VROverlayTemplate asTemplate(){
        if(this instanceof VROverlayTemplate overlayTemplate){
            return overlayTemplate;
        }else{
            return null;
        }
    }

    /**
     * Get overlay name
     *
     * @return the overlay name
     */
    @NotNull
    default Component getName(){
        return Component.literal(getId());
    }

    /**
     * Get overlay description
     *
     * @return the overlay description
     */
    @NotNull
    default Component getDescription(){
        return Component.literal("No description");
    }

    /**
     * Get overlay icon
     * @return resource texture
     */
    default @NotNull GuiTexture getIcon(){
        return getOwner().getAddonIcon();
    }



    //---------------------------
    //--------- OPTIONS ---------
    //---------------------------

    /**
     * Get collection of overlay options
     *
     * <p>
     *     If not empty, the config is created where options will be saved
     * </p>
     *
     * @return the options
     */
    @NotNull
    Collection<OverlayOptionGroup<?>> getOptions();


    /**
     * Get option from id
     *
     * <p>
     *     You can create your own option class,
     *     or use already existing one from {@link org.vmstudio.visor.api.client.gui.overlays.options.types this package}
     * </p>
     *
     * @param id the option id
     *
     * @return option instance or null
     */
    @Nullable
    OverlayOptionGroup<?> getOption(@NotNull String id);

    /**
     * Get option from id
     *
     * <p>
     *     You can create your own option class,
     *     or use already existing one from {@link org.vmstudio.visor.api.client.gui.overlays.options.types this package}
     * </p>
     *
     * @param id the option id
     * @param type the option type
     *
     * @return option instance or null
     */
    @Nullable
    default <T extends OverlayOptionGroup<?>> T getOption(@NotNull String id,
                                                       @NotNull Class<T> type){
        var optionGroup = getOption(id);
        if(type.isInstance(optionGroup)){
            return type.cast(optionGroup);
        }
        return null;
    }

    /**
     * Get config holding
     * saved options of this overlay
     *
     * <p>
     *     if {@link #getOptions()} returns empty list,
     *     the config is not created
     * </p>
     *
     * @return the config file or null
     */
    @Nullable
    ConfigFile getOptionsConfig();

    /**
     * Reloads options from config file
     */
    default void reloadOptions(){
        boolean reloadFile = true;
        for(var option : getOptions()){
            option.loadFromFile(reloadFile);
            reloadFile = false;
        }
    }

    /**
     * Reloads option group of specified id
     * from config file
     */
    default void reloadOption(@NotNull String id){
        var option = getOption(id);
        if(option == null) return;
        option.loadFromFile(true);
    }


    /**
     * If overlay has player modifiable options
     * @return true/false
     */
    default boolean hasModifiableOptions(){
        return getOptions().stream().anyMatch(OverlayOptionGroup::isModifiable);
    }

    //---------------------------------------------
    //--------- FORCED ANCHOR && DRAGGING ---------
    //---------------------------------------------

    /**
     * Get the forced anchor
     *
     * <p>The anchor that is forced and replaces {@link #updatePose(float)}</p>
     *
     * <p>Used in demo overlay</p>
     *
     * @return the forced anchor or null
     */
    @Nullable PoseAnchor getForcedAnchor();

    /**
     * Set the forced anchor
     *
     * <p>The anchor that is forced and replaces {@link #updatePose(float)}</p>
     *
     * <p>Used in demo overlay</p>
     *
     * @param anchor the anchor or null
     */
    void setForcedAnchor(@Nullable PoseAnchor anchor);


    /**
     * Start dragging this overlay with the currently active cursor hand
     */
    void startDragging();

    /**
     * Stop dragging and persist the pose back into pose options when available
     */
    void stopDragging();

    /**
     * If overlay is being dragged rightt now
     *
     * @return true/false
     */
    default boolean isBeingDragged() {
        return false;
    }

    /**
     * If specified raw cursor position is over the drag handle
     *
     * @param rawX raw cursor x
     * @param rawY raw cursor y
     * @return true/false
     */
    default boolean isCursorOnDragHandle(float rawX, float rawY) {
        if (!supportsDragging()) {
            return false;
        }
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return false;
        }
        int edgeX = getCursorBoundsX();
        int edgeY = getCursorBoundsY();
        int edgeWidth = getCursorBoundsWidth();
        int edgeHeight = getCursorBoundsHeight();
        // -1 in any bound means "use the full overlay"
        if (edgeX < 0) edgeX = 0;
        if (edgeY < 0) edgeY = 0;
        if (edgeWidth < 0) edgeWidth = width;
        if (edgeHeight < 0) edgeHeight = height;

        float rawLeftCb  = (float) edgeX / width;
        float rawRightCb = (float) (edgeX + edgeWidth) / width;
        float rawTop     = (float) (edgeY + edgeHeight) / height;
        float rawBottom  = rawTop + 0.15f;

        float barCenterX = (rawLeftCb + rawRightCb) * 0.5f;
        float barHalfW   = (rawRightCb - rawLeftCb) * 0.18f;
        float resizeZoneStart = supportsResizing()
                ? barCenterX + barHalfW + barHalfW * 0.20f
                : rawRightCb;

        return rawX >= rawLeftCb && rawX <= resizeZoneStart
                && rawY > rawTop && rawY <= rawBottom;
    }

    //----------------------------
    //--------- RESIZING ---------
    //----------------------------

    /**
     * Start resizing this overlay with the currently active cursor hand.
     */
    void startResizing();

    /**
     * Stop resizing and persist the new scale into pose options when available.
     */
    void stopResizing();

    /**
     * If overlay is being pinch-resized right now
     *
     * @return true/false
     */
    default boolean isBeingResized() {
        return false;
    }

    /**
     * Lower clamp for the scale during pinch resize.
     *
     * @return min scale (multiplier of base)
     */
    default float getMinScale() {
        return 0.25f;
    }

    /**
     * Upper clamp for the scale during pinch resize.
     *
     * @return max scale (multiplier of base)
     */
    default float getMaxScale() {
        return 4.0f;
    }

    /**
     * If specified raw cursor position is over the resize handle.
     *
     * <p>Default implementation places a small square handle at the right edge
     * of the drag-bar strip (just below the cursor bounds).</p>
     *
     * @param rawX raw cursor x
     * @param rawY raw cursor y
     * @return true/false
     */
    default boolean isCursorOnResizeHandle(float rawX, float rawY) {
        if (!supportsResizing()) {
            return false;
        }
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) {
            return false;
        }
        int edgeX = getCursorBoundsX();
        int edgeY = getCursorBoundsY();
        int edgeWidth = getCursorBoundsWidth();
        int edgeHeight = getCursorBoundsHeight();
        // -1 in any bound means "use the full overlay"
        if (edgeX < 0) edgeX = 0;
        if (edgeY < 0) edgeY = 0;
        if (edgeWidth < 0) edgeWidth = width;
        if (edgeHeight < 0) edgeHeight = height;

        float rawLeftCb  = (float) edgeX / width;
        float rawRightCb = (float) (edgeX + edgeWidth) / width;
        float rawTop     = (float) (edgeY + edgeHeight) / height;
        float rawBottom  = rawTop + 0.15f;

        float barCenterX = (rawLeftCb + rawRightCb) * 0.5f;
        float barHalfW   = (rawRightCb - rawLeftCb) * 0.18f;
        float left  = barCenterX + barHalfW + barHalfW * 0.20f;
        float right = left + barHalfW * 0.55f;

        return rawX >= left && rawX <= right
                && rawY > rawTop && rawY <= rawBottom;
    }


    //--------------------------------------
    //--------- SUPPORTED FEATURES ---------
    //--------------------------------------

    /**
     * If supports update of visibility each render call, instead of tick()
     * @return true/false
     */
    default boolean supportsVisibilityUpdateOnRender(){
        return false;
    }

    /**
     * If overlay is on a HUD layer or on a world layer
     * <p>
     *     HUD layer means the overlay won't be occluded by world objects (blocks, hands, entities etc.)
     * </p>
     * @return true/false
     */
    default boolean isHudLayer(){
        return true;
    }

    /**
     * If overlay is affected by world light level
     *
     *
     * @return true/false
     */
    default boolean supportsLight(){
        return true;
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
     * If overlay can be dragged and repositioned by the player
     *
     * @return true/false
     */
    default boolean supportsDragging() {
        return false;
    }

    /**
     * If overlay can be resized by player
     *
     * @return true/false
     */
    default boolean supportsResizing() {
        return false;
    }

    //----------------------------------------
    //--------- CURSOR && RESOLUTION ---------
    //----------------------------------------

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
     * Get width of an overlay in pixels.
     *
     * @return the width in pixels
     */
    default int getWidth(){
        return VisorAPI.client().getGuiManager().getGuiScaledWidth();
    }

    /**
     * Get height of an overlay in pixels.
     * @return the height in pixels
     */
    default int getHeight(){
        return VisorAPI.client().getGuiManager().getGuiScaledHeight();
    }

    /**
     * Get aspect ratio between {@link #getHeight()} and {@link #getWidth()}
     * <p>
     *     It is a helper method added for convenience
     * </p>
     * @return the aspect ratio
     */
    default float getAspectRatio(){
        return (float) getHeight() / getWidth();
    }

    /**
     * Returns top-left corner X coordinate (in pixels)
     * of the cursor bounds rectangle
     * <p>
     * A return value of {@code -1} indicates that
     * no specific cursor bounds have been set,
     * and the cursor is considered valid across
     * the entire overlay bounds
     * </p>
     *
     * @return the X of the cursor bounds, or {@code -1}
     */
    default int getCursorBoundsX() {
        return 0;
    }

    /**
     * Returns top-left corner Y coordinate (in pixels)
     * of the cursor bounds rectangle
     * <p>
     * A return value of {@code -1} indicates that
     * no specific cursor bounds have been set,
     * and the cursor is considered valid across
     * the entire overlay bounds
     * </p>
     *
     * @return the Y of the cursor bounds, or {@code -1}
     */
    default int getCursorBoundsY() {
        return 0;
    }

    /**
     * Returns the width of the cursor bounds rectangle
     * <p>
     * A return value of {@code -1} indicates that
     * no specific cursor bounds have been set,
     * and the cursor is considered valid across
     * the entire overlay bounds
     * </p>
     *
     * @return the width of the cursor bounds, or {@code -1}
     */
    default int getCursorBoundsWidth() {
        return getWidth();
    }

    /**
     * Returns the height of the cursor bounds rectangle
     * <p>
     * A return value of {@code -1} indicates that
     * no specific cursor bounds have been set,
     * and the cursor is considered valid across
     * the entire overlay bounds
     * </p>
     *
     * @return the height of the cursor bounds, or {@code -1}
     */
    default int getCursorBoundsHeight() {
        return getHeight();
    }

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
     * @param rawX the raw cursor X position relative to overlay bounds
     * @param rawY the raw cursor Y position relative to overlay bounds
     *
     * @return true/false
     */
    default boolean isWithinCursorBounds(float rawX, float rawY) {
        if(rawX < 0f
                || rawX > 1f
                || rawY < 0f
                || rawY > 1f){
            return false;
        }
        int edgeX = getCursorBoundsX();
        int edgeY = getCursorBoundsY();
        int edgeWidth = getCursorBoundsWidth();
        int edgeHeight = getCursorBoundsHeight();
        int width = getWidth();
        int height = getHeight();
        if (width > 0 && height > 0
                && edgeX >= 0 && edgeY >= 0
                && edgeWidth >= 0 && edgeHeight >= 0) {
            int px = (int)(rawX * width);
            int py = (int)(rawY * height);
            return px >= edgeX
                    && px <= edgeX + edgeWidth
                    && py >= edgeY
                    && py <= edgeY + edgeHeight;
        }
        return true;
    }


    //----------------------------
    //--------- MC STUFF ---------
    //----------------------------

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
     * @param mouseX the mouse position X
     * @param mouseY the mouse position Y
     * @param buttonType the button type (0 - left, 1 - right, 3 - scroll)
     *
     * @return if succeeded
     */
    boolean mouseClicked(double mouseX, double mouseY, int buttonType);

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


    //-------------------------
    //--------- EXTRA ---------
    //-------------------------

    /**
     * Override of {@link PrioritySupporter#compareTo(PrioritySupporter)}
     * to sort components in reverse priority order,
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
