package me.phoenixra.visor.api.client.gui.overlays;


import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Helper class for overlays
 */
public class VROverlayHelper {

    private VROverlayHelper() {
        throw new UnsupportedOperationException("This is an utility class and cannot be instantiated");
    }

    /**
     * Convenient if you want to calculate render position
     * once and then just use its room representation,
     * to be relative to room
     * @param overlay the overlay
     * @param overlayScale the new overlay scale
     * @param roomPosition the new position relative to room
     * @param roomRotation the new rotation relative to room
     */
    public static void applyRoomPose(@NotNull VROverlay overlay,
                                     float overlayScale,
                                     @NotNull Vector3fc roomPosition,
                                     @NotNull Matrix4f roomRotation){
        PoseData renderPose = VisorAPI.client().getPlayer()
                .getPoseData(PoseDataType.RENDER);

        Vector3f renderScreenPos = renderPose.convertPositionFrom(
                PoseDataType.ROOM,
                roomPosition
        );
        Matrix4f renderScreenRotation =  renderPose.convertRotationFrom(
                PoseDataType.ROOM,
                roomRotation
        );

        overlay.getPose().update(
                renderScreenPos,
                renderScreenRotation,
                overlayScale
        );
    }

    /**
     * Apply new pose to an overlay with specified pose Anchors
     *
     * <p>You can also adjust offsets for position and rotation</p>
     *
     * <p>If <code>aimRotation</code> is true,
     * the overlay is aimed at rotation anchor.
     * Otherwise, applies same rotation as anchor<p/>
     *
     * @param overlay        The overlay
     * @param positionType   The position anchor
     * @param rotationType   The rotation anchor
     * @param overlayScale   The scale of an overlay quad
     * @param aimRotation    If aim overlay at rotation anchor
     * @param positionOffset The position offset
     * @param rotationOffset The rotation offset
     */
    public static void applyPose(@NotNull VROverlay overlay,
                                 @NotNull PoseAnchor positionType,
                                 @NotNull PoseAnchor rotationType,
                                 float overlayScale,
                                 boolean aimRotation,
                                 @NotNull Vector3fc positionOffset,
                                 @NotNull Vector3fc rotationOffset
    ) {

        PoseData renderPose = VisorAPI.client().getPlayer()
                .getPoseData(PoseDataType.RENDER);

        Vector3f newPosition = positionType.anchorPos(
                renderPose,
                positionOffset
        );
        Matrix4f newRotation;
        if(aimRotation){
            newRotation = rotationType.anchorRotationAim(
                    renderPose,
                    rotationOffset,
                    newPosition
            );
        }else {
            newRotation = rotationType.anchorRotation(
                    renderPose,
                    rotationOffset
            );
        }
        overlay.getPose().update(
                newPosition,
                newRotation,
                overlayScale
        );
    }

    /**
     * Shorter version of {@link #applyPose(VROverlay, PoseAnchor, PoseAnchor, float, boolean, Vector3fc, Vector3fc)}
     *
     * @param overlay        The overlay
     * @param positionType   The position anchor
     * @param rotationType   The rotation anchor
     * @param overlayScale   The scale of an overlay quad
     * @param aimRotation    If aim overlay at rotation anchor
     */
    public static void applyPose(@NotNull VROverlay overlay,
                                 @NotNull PoseAnchor positionType,
                                 @NotNull PoseAnchor rotationType,
                                 float overlayScale,
                                 boolean aimRotation
    ){
        applyPose(
                overlay,
                positionType,
                rotationType,
                overlayScale,
                aimRotation,
                VRMathUtils.ZERO_VECTOR,
                VRMathUtils.ZERO_VECTOR
        );
    }

    /**
     * Anchors one overlay’s chosen point onto another overlay’s chosen point,
     * with optional positional and rotational offsets.
     *
     * @param targetOverlay the overlay to update
     * @param targetNormalX normalized X on the target quad(–1=left, +1=right)
     * @param targetNormalY normalized Y on the target quad(–1=bottom, +1=top)
     * @param targetUseCursorBounds whether to use the target’s cursor bounds
     * @param anchorOverlay the overlay to anchor to
     * @param anchorNormalX normalized X on the anchor quad(–1=left, +1=right)
     * @param anchorNormalY normalized Y on the anchor quad(–1=bottom, +1=top)
     * @param anchorUseCursorBounds whether to use the anchor’s cursor bounds
     * @param positionOffset extra translation in anchor’s local axes
     * @param rotationOffset extra Euler‐angle rotation after anchoring
     */
    public static void anchorWithOverlay(@NotNull VROverlay targetOverlay,
                                         float  targetNormalX,
                                         float  targetNormalY,
                                         boolean targetUseCursorBounds,
                                         @NotNull VROverlay anchorOverlay,
                                         float  anchorNormalX,
                                         float  anchorNormalY,
                                         boolean anchorUseCursorBounds,
                                         @NotNull Vector3fc positionOffset,
                                         @NotNull Vector3fc rotationOffset) {
        VROverlayPose targetPose = targetOverlay.getPose();
        VROverlayPose anchorPose = anchorOverlay.getPose();
        PoseData renderPose  = VisorAPI.client()
                .getPlayer()
                .getPoseData(PoseDataType.RENDER);
        float worldScale = renderPose.getWorldScale();

        Vector3f anchorPointWorld =
                anchorPose.getPositionAt(
                        anchorNormalX,
                        anchorNormalY,
                        anchorUseCursorBounds,
                        PoseDataType.RENDER
                );

        Vector3f originWorld     = new Vector3f(targetPose.getPosition());
        Vector3f targetPointWorld =
                targetPose.getPositionAt(
                        targetNormalX,
                        targetNormalY,
                        targetUseCursorBounds,
                        PoseDataType.RENDER
                );
        Vector3f worldOffsetOld = new Vector3f(targetPointWorld)
                .sub(originWorld);

        Matrix4f invTargetRot = new Matrix4f(targetPose.getRotation()).invert();
        Vector3f localOffset  = invTargetRot.transformDirection(worldOffsetOld, new Vector3f());

        Matrix4f newRot = new Matrix4f(anchorPose.getRotation())
                .mul(
                        new Matrix4f()
                                .rotationZYX(
                                        rotationOffset.z(),
                                        rotationOffset.y(),
                                        rotationOffset.x()
                                ),
                        new Matrix4f()
                );

        Vector3f worldOffsetNew = new Vector3f(localOffset).mul(worldScale);
        newRot.transformDirection(worldOffsetNew);

        Vector3f posOffScaled = new Vector3f(positionOffset).mul(worldScale);
        Vector3f posOffWorld  = new Vector3f(posOffScaled);
        anchorPose.getRotation().transformDirection(posOffWorld);

        Vector3f newOrigin = new Vector3f(anchorPointWorld)
                .add(posOffWorld)
                .sub(worldOffsetNew);

        targetPose.updateOnlyPosAndRotation(
                newOrigin,
                newRot
        );
    }

    /**
     * Anchors one overlay’s chosen point onto another overlay’s chosen point,
     * with optional positional and rotational offsets.
     *
     * @param targetOverlay the overlay to update
     * @param targetNormalX normalized X on the target quad(–1=left, +1=right)
     * @param targetNormalY normalized Y on the target quad(–1=bottom, +1=top)
     * @param targetCustomBounds the custom quad bounds in target overlay pixel units
     *                           (like cursor bounds but customizable).
     *                           Where expected value is: int[4] -> x,y,width,height
     * @param anchorOverlay the overlay to anchor to
     * @param anchorNormalX normalized X on the anchor quad(–1=left, +1=right)
     * @param anchorNormalY normalized Y on the anchor quad(–1=bottom, +1=top)
     * @param anchorCustomBounds the custom quad bounds in anchor overlay pixel units
     *                           (like cursor bounds but customizable).
     *                           Where expected value is: int[4] -> x,y,width,height
     * @param positionOffset extra translation in anchor’s local axes
     * @param rotationOffset extra Euler‐angle rotation after anchoring
     */
    public static void anchorWithOverlay(@NotNull VROverlay targetOverlay,
                                         float  targetNormalX,
                                         float  targetNormalY,
                                         int[] targetCustomBounds,
                                         @NotNull VROverlay anchorOverlay,
                                         float  anchorNormalX,
                                         float  anchorNormalY,
                                         int[] anchorCustomBounds,
                                         @NotNull Vector3fc positionOffset,
                                         @NotNull Vector3fc rotationOffset) {
        VROverlayPose targetPose = targetOverlay.getPose();
        VROverlayPose anchorPose = anchorOverlay.getPose();
        PoseData renderPose  = VisorAPI.client()
                .getPlayer()
                .getPoseData(PoseDataType.RENDER);
        float worldScale = renderPose.getWorldScale();

        Vector3f anchorPointWorld =
                anchorPose.getPositionAt(
                        anchorNormalX,
                        anchorNormalY,
                        anchorCustomBounds,
                        PoseDataType.RENDER
                );

        Vector3f originWorld     = new Vector3f(targetPose.getPosition());
        Vector3f targetPointWorld =
                targetPose.getPositionAt(
                        targetNormalX,
                        targetNormalY,
                        targetCustomBounds,
                        PoseDataType.RENDER
                );
        Vector3f worldOffsetOld = new Vector3f(targetPointWorld)
                .sub(originWorld);

        Matrix4f invTargetRot = new Matrix4f(targetPose.getRotation()).invert();
        Vector3f localOffset  = invTargetRot.transformDirection(worldOffsetOld, new Vector3f());

        Matrix4f newRot = new Matrix4f(anchorPose.getRotation())
                .mul(
                        new Matrix4f()
                                .rotationZYX(
                                        rotationOffset.z(),
                                        rotationOffset.y(),
                                        rotationOffset.x()
                                ),
                        new Matrix4f()
                );

        Vector3f worldOffsetNew = new Vector3f(localOffset).mul(worldScale);
        newRot.transformDirection(worldOffsetNew);

        Vector3f posOffScaled = new Vector3f(positionOffset).mul(worldScale);
        Vector3f posOffWorld  = new Vector3f(posOffScaled);
        anchorPose.getRotation().transformDirection(posOffWorld);

        Vector3f newOrigin = new Vector3f(anchorPointWorld)
                .add(posOffWorld)
                .sub(worldOffsetNew);

        targetPose.updateOnlyPosAndRotation(
                newOrigin,
                newRot
        );
    }

}
