package me.phoenixra.visor.api.client.gui;

import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.gui.overlay.VROverlay;
import me.phoenixra.visor.api.client.gui.overlay.framework.VROverlayScreen;
import me.phoenixra.visor.api.common.ControllerHand;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Handler of GUI cursor
 */
public interface VRCursorHandler {


    /**
     * Get ControllerHand that is considered
     * as a cursor.
     *
     * <p>If {@link #isTwoHandedCursor()} = true, returns last used cursor
     * which is considered to be active and interacts with a GUI</p>
     *
     * @return cursor hand
     */
    @NotNull
    ControllerHand getCursorHand();

    /**
     * Set ControllerHand to be considered as a cursor.
     *
     * @param hand the cursor hand
     */
    void setCursorHand(@NotNull ControllerHand hand);


    /**
     * If both hands are considered as cursor.
     *
     * <p>This might happen when one or both hands are interacting
     * with overlay that supports two cursors {@link VROverlay#supportsTwoHandedCursor()}</p>
     *
     * <p>When true, {@link #getCursorHand()} is considered as active/last used,
     * while the other, inactive cursor is just displayed.
     * <br>Active cursor means, it interacts with GUI</p>
     *
     * @return if both hands are cursor
     */
    boolean isTwoHandedCursor();


    /**
     * Get VROverlay focused by specified cursor hand
     *
     * @param hand cursor hand
     *
     * @return overlay or null
     */
    @Nullable("Not focused, or hand is not a cursor")
    VROverlay getFocusedOverlay(@NotNull ControllerHand hand);

    /**
     * Get VROverlay focused by {@link #getCursorHand()}
     *
     * @return overlay or null
     */
    @Nullable("Not focused")
    default VROverlay getFocusedOverlay(){
        return getFocusedOverlay(getCursorHand());
    }

    /**
     * Get VROverlayScreen focused by specified cursor hand
     *
     * @param hand cursor hand
     *
     * @return overlayScreen or null
     */
    @Nullable("Not focused, or focused at different overlay type, or hand is not a cursor")
    default VROverlayScreen getFocusedOverlayScreen(@NotNull ControllerHand hand){
        if(getFocusedOverlay(hand) instanceof VROverlayScreen overlayScreen){
            return overlayScreen;
        }
        return null;
    }

    /**
     * Get VROverlayScreen focused by {@link #getCursorHand()}
     *
     * @return overlayScreen or null
     */
    @Nullable("Not focused or focused at different overlay type")
    default VROverlayScreen getFocusedOverlayScreen(){
        return getFocusedOverlayScreen(getCursorHand());
    }


    /**
     * If {@link #getCursorHand()} is focused at overlay.
     *
     * @return If focused
     */
    default boolean isCursorHandFocused(){
        return getFocusedOverlay(getCursorHand()) != null;
    }

    /**
     * If specified hand is focused at overlay.
     *
     * @return If focused
     */
    default boolean isHandFocused(@NotNull ControllerHand hand){
        return getFocusedOverlay(hand) != null;
    }

    /**
     * If any hand or both are focused at overlay.
     *
     * @return If focused
     */
    default boolean isAnyHandFocused(){
        return isHandFocused(ControllerHand.MAIN)
                || isHandFocused(ControllerHand.OFFHAND);
    }


    boolean isDraggingItem();

    @ApiStatus.Internal
    void setDraggingItem(boolean flag);


    /**
     * Get cursor line length for specified hand.<br>
     * Cursor line length is a distance from hand
     * to a collision point with focused overlay.
     *
     * <p>If not focused at overlay, returns '-1'</p>
     *
     * @param hand cursor hand
     * @return length of a cursor line
     */
    double getCursorLineLength(@NotNull ControllerHand hand);


    boolean isFacingOverlay(PoseElement element,
                            VROverlay overlay,
                            boolean checkUpsideDown,
                            double threshold
    );

    default boolean isFacingOverlay(PoseElement element,
                                    VROverlay overlay,
                                    boolean checkUpsideDown
    ) {
        return isFacingOverlay(
                element, overlay,
                checkUpsideDown,0.2
        );
    }


    /**
     *
     * @param component
     * @param guiPosition
     * @param guiRotation
     * @param guiScale
     * @return
     */
    @NotNull
    Vector2f findCursorPosition2D(@NotNull PoseElement component,
                                    @NotNull Vector3fc guiPosition,
                                    @NotNull Matrix4fc guiRotation,
                                    float guiScale);



    Vector3f findCursorPosition3D(@NotNull PoseElement component,
                              @NotNull Vector3fc guiPosition,
                              @NotNull Matrix4fc guiRotation,
                              float guiScale);


}
