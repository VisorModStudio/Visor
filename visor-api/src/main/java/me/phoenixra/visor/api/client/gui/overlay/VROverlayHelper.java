package me.phoenixra.visor.api.client.gui.overlay;


import me.phoenixra.visor.api.VisorAPI;
import me.phoenixra.visor.api.client.data.PoseAnchor;
import me.phoenixra.visor.api.client.data.PoseData;
import me.phoenixra.visor.api.client.data.PoseDataType;
import me.phoenixra.visor.api.common.utils.VRMathUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
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
                .getPose(PoseDataType.RENDER);

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
     * Render image
     *
     * @param guiGraphics the gfx
     * @param textureLocation the image location
     * @param posX the position X
     * @param posY the position Y
     * @param width the width
     * @param height the height
     * @param textureWidth the texture width
     * @param textureHeight the texture height
     */
    public static void renderImage(GuiGraphics guiGraphics,
                                   ResourceLocation textureLocation,
                                   int posX, int posY,
                                   int width, int height,
                                   int textureWidth, int textureHeight) {
        guiGraphics.blit(
                textureLocation,
                posX, posY,         // screen x, y
                0,                  // z-depth
                0.0F, 0.0F,         // texture u, v
                width, height,      // area to draw (width, height in pixels)
                textureWidth, textureHeight  // full texture size
        );
    }

}
