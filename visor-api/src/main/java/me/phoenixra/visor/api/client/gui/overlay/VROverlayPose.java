package me.phoenixra.visor.api.client.gui.overlay;

import lombok.*;
import me.phoenixra.atumconfig.api.tuples.PairRecord;
import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;


/**
 * Holder of overlay pose data,
 * relative to world render coordinates. {@link PoseDataType#RENDER}
 */
@Getter @EqualsAndHashCode @ToString
public class VROverlayPose {

    /**
     * Basic scale of an overlay quad in world
     */
    public static final float QUAD_SCALE = 1.5f;

    /**
     * Get Overlay position of {@link PoseDataType#RENDER} type
     */
    private Vector3fc position = new Vector3f(0f, 0f, 0f);

    /**
     * Get Overlay rotation of {@link PoseDataType#RENDER} type
     */
    private Matrix4fc rotation = new Matrix4f();

    /**
     * Get Overlay scale
     */
    private float scale;

    /**
     * Get Overlay top-left corner position of {@link PoseDataType#RENDER} type
     */
    private Vector3fc topLeftCorner = new Vector3f(0f, 0f, 0f);

    /**
     * Get Overlay bottom-right corner position of {@link PoseDataType#RENDER} type
     */
    private Vector3fc bottomRightCorner = new Vector3f(0f, 0f, 0f);

    /**
     *
     * @param overlayScale the overlay scale
     */
    public VROverlayPose(float overlayScale){
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

        var corners = calcOverlayCorners();
        this.topLeftCorner = corners.first();
        this.bottomRightCorner = corners.second();
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



    private PairRecord<Vector3f, Vector3f> calcOverlayCorners() {

        PoseData pose = VisorAPI.client().getPlayer().getPose(PoseDataType.RENDER);
        float worldScale = pose.getWorldScale();

        float effectiveScale = QUAD_SCALE * scale * worldScale;

        float aspect = VisorAPI.client().getGuiManager().getScaledAspectRatio();

        Vector3fc right = new Vector3f(
                VRMathUtils.extractRightDir(rotation, true)
        );
        Vector3fc up    = new Vector3f(
                VRMathUtils.extractUpDir(rotation, true)
        );

        float halfW = effectiveScale * 0.5f;
        float halfH = effectiveScale * aspect * 0.5f;

        Vector3fc center = new Vector3f(position);

        Vector3f topLeft = new Vector3f(center)
                .sub(right.mul(halfW, new Vector3f()))
                .add(up.mul(halfH, new Vector3f()));

        Vector3f bottomRight = new Vector3f(center)
                .add(right.mul(halfW, new Vector3f()))
                .sub(up.mul(halfH, new Vector3f()));

        return new PairRecord<>(topLeft, bottomRight);
    }
}
