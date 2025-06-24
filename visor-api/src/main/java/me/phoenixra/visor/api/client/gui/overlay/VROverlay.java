package me.phoenixra.visor.api.client.gui.overlay;

import com.mojang.blaze3d.pipeline.RenderTarget;
import me.phoenixra.atumconfig.api.config.ConfigFile;
import me.phoenixra.visor.api.client.gui.overlay.options.OverlayOptionCategory;
import me.phoenixra.visor.api.client.gui.overlay.framework.OverlayCursorData;
import me.phoenixra.visor.api.common.addon.ElementPriority;
import me.phoenixra.visor.api.common.addon.PrioritySupporter;
import me.phoenixra.visor.api.common.addon.VisorElement;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Collection;

public interface VROverlay extends VisorElement, PrioritySupporter {


    void tick();

    void applyModelView(float partialTick);

    void onEnable();
    void onDisable();


    void setOverlayScale(float value);

    @Nullable
    <T extends OverlayOptionCategory> T getOptionCategory(@NotNull Class<T> type);

    @NotNull
    OverlayCursorData getActiveCursorData();

    @NotNull
    OverlayCursorData getInactiveCursorData();


    /**
     * Get render target
     * <br>
     * (used in rendering to display
     * overlay separately from other staff)
     *
     * @return the render target
     */
    @Nullable
    RenderTarget getRenderTarget();

    @NotNull
    ConfigFile getConfig();
    /**
     * Get overlay position
     *
     * @return the mc 3d vector
     */
    @NotNull
    Vector3fc getPosition();

    /**
     * Get overlay rotation
     *
     * @return the VR matrix
     */
    @NotNull
    Matrix4fc getRotation();

    /**
     * If displays both cursors
     *
     * @return true/false
     */
    default boolean supportsTwoHandedCursor(){
        return false;
    }


    /**
     * Get mouse position X
     *
     * @return The integer from 0 to width of an overlay
     */
    int getMouseX();

    /**
     * Get mouse position Y
     *
     * @return The integer from 0 to height of an overlay
     */
    int getMouseY();


    float getRawCursorX();

    float getRawCursorY();

    @ApiStatus.Internal
    void updateMousePosition(boolean activeCursorHand, float rawX, float rawY);


    boolean mouseClicked(double x, double y, int buttonType);

    boolean mouseReleased(double x, double y, int buttonType);

    void mouseMoved(double x, double y);

    boolean mouseDragged(double mouseX, double mouseY,
                         int button,
                         double deltaX, double deltaY);

    boolean mouseScrolled(double mouseX, double mouseY, double scrollDelta);

    boolean keyPressed(int keyCode, int keyScan, int modifiers);

    boolean keyReleased(int keyCode, int keyScan, int modifiers);

    boolean charTyped(char chr, int modifiers);


    /**
     * If should render an enabled overlay.
     * <br><br>
     * Highly suggested to return false when an overlay <br>
     * displays nothing while being enabled<br>
     * to reduce the amount of resources used.<br>
     *
     * @return If overlay is visible
     */
    boolean isVisible();

    /**
     * If overlay is created from config
     * @return
     */
    default boolean isConfigOverlay() {
        return getOverlayType() != null;
    }

    /**
     * Get overlay type if created from config
     * @return
     */
    @Nullable String getOverlayType();

    @ApiStatus.Internal
    default boolean isCursorWithinBounds(boolean mainCursor, float rawX, float rawY) {
        return rawX > 0f
                && rawX < 1f
                && rawY > 0f
                && rawY < 1f;
    }

    /**
     * Should cursor handler IGNORE
     * aim collision with this overlay?
     *
     * @return true/false
     */
    default boolean isCursorSupported() {
        return true;
    }

    /**
     * Should cursorHandler ignore whether
     * cursor device is faced towards an overlay
     * <br>
     *
     * @return true/false
     */
    default boolean ignoreFacingGui() {
        return false;
    }



    /**
     * Get overlay scale.
     *
     * @return The positive, non-zero float number
     */
    float getOverlayScale();


    /**
     * Set overlay position relative to VR room
     *
     * @param value the mc 3d vector
     */
    void setPosition(@NotNull Vector3fc value);

    /**
     * Set overlay rotation relative to VR room
     *
     * @param value the VR matrix
     */
    void setRotation(@NotNull Matrix4f value);



    @NotNull
    String getDisplayName();

    Collection<OverlayOptionCategory> getOptionsList();
}
