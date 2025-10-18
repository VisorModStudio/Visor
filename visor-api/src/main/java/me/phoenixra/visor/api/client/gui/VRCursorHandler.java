package me.phoenixra.visor.api.client.gui;

import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseElement;
import me.phoenixra.visor.api.client.gui.overlays.VROverlay;
import me.phoenixra.visor.api.client.gui.overlays.framework.VROverlayScreen;
import me.phoenixra.visor.api.common.ControllerHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.function.Function;

/**
 * Handler of VR GUI cursor
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
     * Get force focused overlay
     *
     * @return force focused overlay
     */
    @Nullable VROverlay getForceFocused();

    /**
     * Set force focused overlay<br>
     *
     * <p>Force focused overlay is prioritized,
     * and any other overlay closer to cursor is ignored.</p>
     *
     * <p>Force focus will be lost if cursor
     * is not focusing this overlay at all</p>
     *
     * <p>This feature can be used for example
     * in case, where your overlay is being dragged by hand,
     * and you don't want other overlays to interfere</p>
     *
     * @param overlay the overlay to force focus on
     */
    void setForceFocused(@Nullable VROverlay overlay);


    /**
     * If both hands are considered as cursor.
     *
     * <p>This might happen when one or both hands are focused
     * at overlay which supports two cursors {@link VROverlay#supportsTwoCursors()}</p>
     *
     * <p>When true, {@link #getCursorHand()} is considered as active/last used,
     * while the other, inactive cursor is just displayed.</p>
     *
     * <p>Active cursor means, it interacts with GUI</p>
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


    /**
     *
     * @param cursorPos position of a cursor on overlay
     * @param focusedOverlay overlay that is found to be focused
     */
    record CursorResult(@NotNull Vector3fc cursorPos,
                        @Nullable VROverlay focusedOverlay) { }

    /**
     *
     * @param hand the hand to check
     * @param poseData the pose data to use
     * @param overlayFilter the filter for overlays that will be checked. Null for all
     * @param checkForceFocused if {@link #getForceFocused()} should affect on a result
     * @return result
     */
    @NotNull CursorResult getCursorResult(@NotNull ControllerHand hand,
                                          @NotNull PoseData poseData,
                                          @Nullable Function<VROverlay, Boolean> overlayFilter,
                                          boolean checkForceFocused);
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




    /**
     * If {@code element} is considered to be facing {@code overlay}
     *
     * @param element  the pose element
     * @param overlay  overlay that should be faced
     *
     * @param checkUpsideDown  if {@code true}, rejects 180°-flipped orientations
     *
     * @return If facing
     *
     */
    boolean isFacingOverlay(@NotNull PoseElement element,
                                    @NotNull VROverlay overlay,
                                    boolean checkUpsideDown);


    /**
     * Computes the cursor’s position in GUI for the given
     * {@code element}.
     *
     * @param element     pose element whose cursor is queried
     * @param guiPosition position of the GUI
     * @param guiRotation rotation of the GUI
     * @param guiScale    scale factor applied to the GUI
     * @param guiAspectRatio the aspect ratio between width and height of the gui
     *
     * @return Vector where x,y are cursor coordinates and z is cursor length
     */
    @NotNull Vector3f findCursorPosition3D(@NotNull PoseElement element,
                                           @NotNull Vector3fc guiPosition,
                                           @NotNull Matrix4fc guiRotation,
                                           float guiScale,
                                           float guiAspectRatio);

    /**
     * Computes the cursor’s position in GUI for the given
     * {@code element}.
     *
     * @param element     pose element whose cursor is queried
     * @param guiPosition position of the GUI
     * @param guiRotation rotation of the GUI
     * @param guiScale    scale factor applied to the GUI
     * @param guiAspectRatio the aspect ratio between width and height of the gui
     *
     * @return Vector where x,y are cursor coordinates
     */
    default @NotNull Vector2f findCursorPosition2D(@NotNull PoseElement element,
                                                   @NotNull Vector3fc guiPosition,
                                                   @NotNull Matrix4fc guiRotation,
                                                   float guiScale,
                                                   float guiAspectRatio) {
        var vec3 = findCursorPosition3D(
                element,
                guiPosition, guiRotation,
                guiScale,
                guiAspectRatio
        );
        return new Vector2f(vec3.x, vec3.y);
    }


}
