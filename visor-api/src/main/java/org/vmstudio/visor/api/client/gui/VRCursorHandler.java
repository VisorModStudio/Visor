package org.vmstudio.visor.api.client.gui;

import org.vmstudio.visor.api.client.events.gui.CursorFocusChangedVREvent;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.common.player.VRPose;
import org.vmstudio.visor.api.client.gui.overlays.VROverlay;
import org.vmstudio.visor.api.client.gui.overlays.framework.VROverlayScreen;
import org.vmstudio.visor.api.common.HandType;
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
     * Get hand type that is considered
     * as a cursor.
     *
     * <p>If {@link #isTwoHandedCursor()} = true, returns last used cursor
     * which is considered to be active and interacts with a GUI</p>
     *
     * @return cursor hand
     */
    @NotNull
    HandType getCursorHand();

    /**
     * Set hand type to be considered as a cursor.
     *
     * @param hand the cursor hand
     */
    void setCursorHand(@NotNull HandType hand);


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
     * and any other overlays closer to cursor are ignored.</p>
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
     * @param activeCursor if check whether cursor is active.
     *                     When false, the method will return true even if hand just aimed at overlay
     *
     * @return overlay or null
     */
    @Nullable("Not focused, or hand is not a cursor")
    VROverlay getFocusedOverlay(@NotNull HandType hand, boolean activeCursor);

    /**
     * Overload of {@link #getFocusedOverlay(HandType, boolean)} with activeCursor = false
     *
     * @param hand cursor hand
     *
     * @return overlay or null
     */
    @Nullable("Not focused, or hand is not a cursor")
    default VROverlay getFocusedOverlay(@NotNull HandType hand){
        return getFocusedOverlay(hand, false);
    }

    /**
     * Get VROverlay focused by {@link #getCursorHand()}
     *
     * @param activeCursor if check whether cursor is active.
     *                     When false, the method will return true even if hand just aimed at overlay
     *
     * @return overlay or null
     */
    @Nullable("Not focused")
    default VROverlay getFocusedOverlay(boolean activeCursor){
        return getFocusedOverlay(getCursorHand(), activeCursor);
    }
    /**
     * Overload of {@link #getFocusedOverlay(boolean)} with activeCursor = false
     *
     * @return overlay or null
     */
    @Nullable("Not focused")
    default VROverlay getFocusedOverlay(){
        return getFocusedOverlay(false);
    }

    /**
     * Get VROverlayScreen focused by specified cursor hand
     *
     * @param hand cursor hand
     * @param activeCursor if check whether cursor is active.
     *                     When false, the method will return true even if hand just aimed at overlay
     *
     * @return overlayScreen or null
     */
    @Nullable("Not focused, or focused at different overlay type, or hand is not a cursor")
    default VROverlayScreen getFocusedOverlayScreen(@NotNull HandType hand, boolean activeCursor){
        if(getFocusedOverlay(hand, activeCursor) instanceof VROverlayScreen overlayScreen){
            return overlayScreen;
        }
        return null;
    }
    /**
     * Overload of {@link #getFocusedOverlayScreen(HandType, boolean)} with activeCursor = false
     *
     * @param hand cursor hand
     * @return overlayScreen or null
     */
    @Nullable("Not focused, or focused at different overlay type, or hand is not a cursor")
    default VROverlayScreen getFocusedOverlayScreen(@NotNull HandType hand){
        return getFocusedOverlayScreen(hand, false);
    }

    /**
     * Get VROverlayScreen focused by {@link #getCursorHand()}
     *
     * @param activeCursor if check whether cursor is active.
     *                     When false, the method will return true even if hand just aimed at overlay
     *
     * @return overlayScreen or null
     */
    @Nullable("Not focused or focused at different overlay type")
    default VROverlayScreen getFocusedOverlayScreen(boolean activeCursor){
        return getFocusedOverlayScreen(getCursorHand(), activeCursor);
    }
    /**
     * Overload of {@link #getFocusedOverlayScreen(boolean)} with activeCursor = false
     *
     * @return overlayScreen or null
     */
    @Nullable("Not focused or focused at different overlay type")
    default VROverlayScreen getFocusedOverlayScreen(){
        return getFocusedOverlayScreen(getCursorHand(), false);
    }

    /**
     * Request clearance of an overlay focus from hand
     * <p>
     *     Clear will be made on next cursor state update
     * </p>
     *
     * In case you need to block focus, use this method with {@link CursorFocusChangedVREvent this cancellable event}
     *
     * @param handType the hand type
     */
    void clearFocus(@NotNull HandType handType);

    /**
     * If {@link #getCursorHand()} is focused at overlay.
     *
     * @param activeCursor if check whether cursor is active.
     *                     When false, the method will return true even if hand just aimed at overlay
     *
     * @return If focused
     */
    default boolean isCursorHandFocused(boolean activeCursor){
        return getFocusedOverlay(getCursorHand(), activeCursor) != null;
    }
    /**
     * Overload of {@link #isCursorHandFocused(boolean)} with activeCursor = false
     *
     * @return If focused
     */
    default boolean isCursorHandFocused(){
        return isCursorHandFocused(false);
    }

    /**
     * If specified hand is focused at overlay.
     *
     * @param hand cursor hand
     * @param activeCursor if check whether cursor is active.
     *                     When false, the method will return true even if hand just aimed at overlay
     * @return If focused
     */
    default boolean isHandFocused(@NotNull HandType hand, boolean activeCursor){
        return getFocusedOverlay(hand, activeCursor) != null;
    }
    /**
     * Overload of {@link #isHandFocused(HandType, boolean)} with activeCursor = false
     *
     * @param hand cursor hand
     * @return If focused
     */
    default boolean isHandFocused(@NotNull HandType hand){
        return isHandFocused(hand, false);
    }

    /**
     * If any hand or both are focused at overlay.
     *
     * @param activeCursor if check whether cursor is active.
     *                     When false, the method will return true even if hand just aimed at overlay
     *
     * @return If focused
     */
    default boolean isAnyHandFocused(boolean activeCursor){
        return isHandFocused(HandType.MAIN, activeCursor)
                || isHandFocused(HandType.OFFHAND, activeCursor);
    }
    /**
     * Overload of {@link #isAnyHandFocused(boolean)} with activeCursor = false
     *
     * @return If focused
     */
    default boolean isAnyHandFocused(){
        return isAnyHandFocused(false);
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
    @NotNull CursorResult getCursorResult(@NotNull HandType hand,
                                          @NotNull VRPlayerPoseClient poseData,
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
    double getCursorLineLength(@NotNull HandType hand);




    /**
     * If {@code element} is considered to be facing {@code overlay}
     *
     * @param pose  the pose
     * @param overlay  overlay that should be faced
     *
     * @param checkUpsideDown  if {@code true}, rejects 180°-flipped orientations
     *
     * @return If facing
     *
     */
    boolean isFacingOverlay(@NotNull VRPose pose,
                            @NotNull VROverlay overlay,
                            boolean checkUpsideDown);


    /**
     * Computes the cursor’s position in GUI for the given
     * {@code element}.
     *
     * @param pose     pose whose cursor is queried
     * @param guiPosition position of the GUI
     * @param guiRotation rotation of the GUI
     * @param guiScale    scale factor applied to the GUI
     * @param guiAspectRatio the aspect ratio between height and width of the gui
     *
     * @return Vector where x,y are cursor coordinates and z is cursor length
     */
    @NotNull Vector3f findCursorPosition3D(@NotNull VRPose pose,
                                           @NotNull Vector3fc guiPosition,
                                           @NotNull Matrix4fc guiRotation,
                                           float guiScale,
                                           float guiAspectRatio);

    /**
     * Computes the cursor’s position in GUI for the given
     * {@code element}.
     *
     * @param pose        pose whose cursor is queried
     * @param guiPosition position of the GUI
     * @param guiRotation rotation of the GUI
     * @param guiScale    scale factor applied to the GUI
     * @param guiAspectRatio the aspect ratio between height and width of the gui
     *
     * @return Vector where x,y are cursor coordinates
     */
    default @NotNull Vector2f findCursorPosition2D(@NotNull VRPose pose,
                                                   @NotNull Vector3fc guiPosition,
                                                   @NotNull Matrix4fc guiRotation,
                                                   float guiScale,
                                                   float guiAspectRatio) {
        var vec3 = findCursorPosition3D(
                pose,
                guiPosition, guiRotation,
                guiScale,
                guiAspectRatio
        );
        return new Vector2f(vec3.x, vec3.y);
    }


}
