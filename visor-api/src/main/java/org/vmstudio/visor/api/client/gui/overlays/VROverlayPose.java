package org.vmstudio.visor.api.client.gui.overlays;

import lombok.*;
import org.vmstudio.visor.api.VisorAPI;
import org.vmstudio.visor.api.client.player.pose.VRPlayerPoseClient;
import org.vmstudio.visor.api.client.player.pose.PlayerPoseType;
import org.vmstudio.visor.api.common.utils.VRMathUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.*;


/**
 * Holder of overlay pose data,
 * relative to world render coordinates. {@link PlayerPoseType#RENDER}
 */
@EqualsAndHashCode @ToString
public class VROverlayPose {

    /**
     * Basic scale of an overlay quad in world
     */
    public static final float QUAD_SCALE = 1.5f;



    @Getter
    private final VROverlay owner;

    /**
     * Get Overlay position of {@link PlayerPoseType#RENDER} type
     */
    @Getter
    private Vector3fc position = new Vector3f(0f, 0f, 0f);

    /**
     * Get Overlay rotation of {@link PlayerPoseType#RENDER} type
     */
    @Getter
    private Matrix4fc rotation = new Matrix4f();

    /**
     * Get Overlay scale
     */
    @Getter
    private float scale;


    private final Vector3f rightDir = new Vector3f();
    private final Vector3f upDir = new Vector3f();
    @Getter
    private float halfWidth;
    @Getter
    private float halfHeight;

    /**
     *
     * @param overlayScale the overlay scale
     */
    public VROverlayPose(@NotNull VROverlay owner, float overlayScale){
        this.owner = owner;
        this.scale = overlayScale;
    }

    /**
     * Update pose data.
     *
     * <p>To not cause issues, it is highly recommended to call update
     * only at the beginning of {@link VROverlay#tick()}(preTick),
     * or in {@link VROverlay#updatePose(float)}</p>
     *
     * @param position the new overlay position
     * @param rotation the new overlay rotation
     * @param overlayScale the new overlay scale
     */
    public void update(@NotNull Vector3fc position,
                       @NotNull Matrix4fc rotation,
                       float overlayScale){
        this.position = position;
        this.rotation = rotation;
        this.scale = overlayScale;


        VRPlayerPoseClient renderPose = VisorAPI.client().getVRLocalPlayer().getPoseData(PlayerPoseType.RENDER);
        float worldScale = renderPose.getWorldScale();
        float effectiveScale = QUAD_SCALE * scale * worldScale;
        float aspect = owner.getAspectRatio();

        this.halfWidth  = effectiveScale * 0.5f;
        this.halfHeight = effectiveScale * aspect * 0.5f;
        this.rightDir.set(VRMathUtils.extractRightDir(rotation, true));
        this.upDir.set(VRMathUtils.extractUpDir   (rotation, true));

    }

    /**
     * Update only position and rotation.
     *
     * <p>Shorter version of {@link #update(Vector3fc, Matrix4fc, float)}</p>
     *
     * @param position the new overlay position
     * @param rotation the new overlay rotation
     */
    public void updateOnlyPosAndRotation(@NotNull Vector3fc position,
                                              @NotNull Matrix4fc rotation) {
        update(position, rotation, scale);
    }

    /**
     * Update only position.
     *
     * <p>Shorter version of {@link #update(Vector3fc, Matrix4fc, float)}</p>

     * @param position the new overlay position
     */
    public void updateOnlyPosition(@NotNull Vector3fc position){
        update(position, rotation, scale);
    }

    /**
     * Update only rotation
     *
     * <p>Shorter version of {@link #update(Vector3fc, Matrix4fc, float)}</p>
     *
     * @param rotation the new overlay rotation
     */
    public void updateOnlyRotation(@NotNull Matrix4fc rotation){
        update(position, rotation, scale);
    }

    /**
     * Update only scale
     *
     * <p>Shorter version of {@link #update(Vector3fc, Matrix4fc, float)}</p>
     *
     * @param overlayScale the new overlay scale
     */
    public void updateOnlyScale(float overlayScale){
        update(position, rotation, overlayScale);
    }


    /**
     * Returns a point on the overlay quad
     * from normalized coordinates
     * <p>
     *     The returned result
     *     is of specified {@link PlayerPoseType pose type}
     * </p>
     *
     * @param returnType the PoseDataType in whose coordinate system to express the result
     * @param useCursorBounds if to use cursor bounds as edges of quad
     * @param xNorm      -1.0 (left) to +1.0 (right)
     * @param yNorm      -1.0 (bottom) to +1.0 (top)
     * @return position in the specified coordinate system
     */
    public Vector3f getPositionAt(float xNorm, float yNorm,
                                  boolean useCursorBounds,
                                  @NotNull PlayerPoseType returnType) {
        if(useCursorBounds){
            int w = owner.getWidth();
            int h = owner.getHeight();
            int edgeX  = owner.getCursorBoundsX();
            int edgeY  = owner.getCursorBoundsY();
            int edgeW  = owner.getCursorBoundsWidth();
            int edgeH  = owner.getCursorBoundsHeight();
            if (w > 0 && h > 0
                    && edgeX >= 0 && edgeY >= 0
                    && edgeW >  0 && edgeH >  0) {
                float localX = (xNorm + 1f) * 0.5f;
                float localY = (yNorm + 1f) * 0.5f;

                float pixelX = edgeX + localX * edgeW;
                float pixelY = edgeY + (1f - localY) * edgeH;

                xNorm = (pixelX / w) * 2f - 1f;
                yNorm = 1f - (pixelY / h) * 2f;
            }
        }
        Vector3f pointInRender = new Vector3f(position)
                .add(new Vector3f(rightDir).mul(halfWidth  * xNorm,
                        new Vector3f()))
                .add(new Vector3f(upDir).mul(halfHeight * yNorm,
                        new Vector3f()));
        VRPlayerPoseClient targetPose = VisorAPI.client().getVRLocalPlayer().getPoseData(returnType);
        return targetPose.convertPositionFrom(PlayerPoseType.RENDER, pointInRender);
    }

    /**
     * Returns a point on the overlay quad
     * from normalized coordinates
     * <p>
     *     The returned result
     *     is of specified {@link PlayerPoseType pose type}
     * </p>
     *
     * @param returnType the PoseDataType in whose coordinate system to express the result
     * @param customBounds the custom quad bounds in overlay pixel units
     *                     (like cursor bounds but customizable).
     *                     Where expected value is: int[4] -> x,y,width,height
     * @param xNorm      -1.0 (left) to +1.0 (right)
     * @param yNorm      -1.0 (bottom) to +1.0 (top)
     * @return position in the specified coordinate system
     */
    public Vector3f getPositionAt(float xNorm, float yNorm,
                                  int[] customBounds,
                                  @NotNull PlayerPoseType returnType) {
        if(customBounds != null){
            int w = owner.getWidth();
            int h = owner.getHeight();
            int edgeX  = customBounds[0];
            int edgeY  = customBounds[1];
            int edgeW  = customBounds[2];
            int edgeH  = customBounds[3];
            if (w > 0 && h > 0
                    && edgeX >= 0 && edgeY >= 0
                    && edgeW >  0 && edgeH >  0) {
                float localX = (xNorm + 1f) * 0.5f;
                float localY = (yNorm + 1f) * 0.5f;

                float pixelX = edgeX + localX * edgeW;
                float pixelY = edgeY + (1f - localY) * edgeH;

                xNorm = (pixelX / w) * 2f - 1f;
                yNorm = 1f - (pixelY / h) * 2f;
            }
        }
        Vector3f pointInRender = new Vector3f(position)
                .add(new Vector3f(rightDir).mul(halfWidth  * xNorm,
                        new Vector3f()))
                .add(new Vector3f(upDir).mul(halfHeight * yNorm,
                        new Vector3f()));
        VRPlayerPoseClient targetPose = VisorAPI.client().getVRLocalPlayer().getPoseData(returnType);
        return targetPose.convertPositionFrom(PlayerPoseType.RENDER, pointInRender);
    }
}
