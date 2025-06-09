package me.phoenixra.visor.api.client.gui;

import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.types.VROverlayScreen;
import me.phoenixra.visor.api.common.ControllerHand;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;


/**
 * This class handles Gui Cursor behavior
 */
public interface VRCursorHandler {

    /**
     * Returns Currently used hand as a cursor <br>
     * OR NULL if client not using any GUI at the moment
     *
     * @return ControllerHand type or null
     */
    @Nullable
    ControllerHand getCursorHand();

    /**
     * Get Length of a cursor line.<br>
     * OR -1 if client not using any GUI at the moment
     * <br><br>
     * Cursor line is drawn between client hand and GUI<br>
     *
     * @return cursor line length
     */
    double getCursorDisplayLength();

    /**
     * Get Currently focused overlay
     * or NULL
     * @return focused overlay or null
     */
    @Nullable
    VROverlay getFocusedOverlay();

    @Nullable
    VROverlayScreen getFocusedOverlayAsScreen();


    boolean isBothCursorsDisplayed();

    /**
     *  Get second cursor length
     *  (when both cursors displayed)
     */
    double getCursorDisplayLength2();

    /**
     *
     * @return If currently dragging item in hand
     */
    boolean isDraggingItem();

    @ApiStatus.Internal
    void setDraggingItem(boolean flag);

    /**
     * If player component aimed at overlay.<br>
     * Ignores other overlays and game screen collisions
     *
     * @param overlay the overlay<br>
     * @param component the player component<br>
     * @param checkUpsideDown return false if
     *                        component positioned upside down
     *                        towards an overlay<br>
     * @param overlayBoundsExtraX value added to width bounds
     *                            (adds given value divided by
     *                            2 for both sides)<br>
     * @param overlayBoundsExtraY value added to height bounds
     *                            (adds given value divided by
     *                            2 for both sides)
     * @return if aimed
     */
    boolean isComponentAimedAtOverlay(@NotNull VROverlay overlay,
                                      @NotNull PoseElement component,
                                      boolean checkUpsideDown,
                                      float overlayBoundsExtraX,
                                      float overlayBoundsExtraY);

    /**
     * Returns cursor coordinates, calculated
     * from player component aim collision position at GUI
     *
     * @param component player component to get aim from
     * @param guiPosRoom gui position in room
     * @param guiRotationRoom gui rotation matrix in room
     * @param guiScale gui scale
     * @return x,y. if aim collision is within gui bounds, the values
     * will be within [0,1] bounds
     */
    @NotNull
    Vec2 getCursorCoordsInGui(@NotNull PoseElement component,
                              @NotNull Vec3 guiPosRoom,
                              @NotNull Matrix4fc guiRotationRoom,
                              float guiScale);

    /**
     * Returns cursor coordinates, calculated
     * from player component aim collision position at GUI<br><br>
     *
     * Additionally, returns Z value
     * (distance between component and aim collision)
     * @param component player component to get aim from
     * @param guiPosRoom gui position in room
     * @param guiRotationRoom gui rotation matrix in room
     * @param guiScale gui scale
     * @return x,y,z. if aim collision is within gui bounds, the x,y values
     * will be within [0,1] bounds
     */
    Vec3 getCursorCoordsInGuiWithDepth(@NotNull PoseElement component,
                                       @NotNull Vec3 guiPosRoom,
                                       @NotNull Matrix4fc guiRotationRoom,
                                       float guiScale);

    /**
     * @return If cursor focused at overlay or game screen
     */
    boolean isCursorFocused();

}
